package org.aimas.consert.middleware.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.ManagerSpecification;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.protocol.ContextSubscriptionRequest;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

/**
 * Implementation of the interface for the API
 */
public class MiddlewareAPIImpl implements MiddlewareAPI {
	
	// route where the engine answers to queries
	private final static String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.ENGINE_ROUTE + "/anwer_query/"; 
	
	// route for context subscriptions
	private final static String CONTEXT_SUBSCRIPTION_ROUTE =  RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfigV1.DISSEMINATION_ROUTE + "/context_subscriptions/";
	
	// route for context assertion capabilities
	private final static String ASSERTION_CAPABILITIES_ROUTE =  RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfigV1.COORDINATION_ROUTE + "/context_assertions/";
	
	
	private final static IRI RDF_TYPE = SimpleValueFactory.getInstance()
			.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	
	private final static IRI ASSERTION_CAPABILITY_IRI = SimpleValueFactory.getInstance()
			.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability");
	
	
	private Repository convRepo;  // repository used to convert RDF statements and Java objects
	private RepositoryConnection convRepoConn;  // connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository
	private HttpClient client;  // client to use for the communications with the agents
	
	private AgentAddress orgMgrConfig;  // configuration for communications with the OrgMgr
	private AgentAddress engineConfig;  // configuration for communications with the CONSERT Engine
	private AgentAddress ctxQueryHandlerConfig;  // configuration for communications with the CtxQueryHandler
	private AgentAddress ctxCoordConfig;  // configuration for communications with the CtxCoord
	

	public MiddlewareAPIImpl() {
		
		// Initialization of the conversion repository
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);
		
		this.client = Vertx.vertx().createHttpClient();
		
		// Get configuration of OrgMgr
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(ManagerSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#OrgMgrSpec");
		CMMAgentContainer container = orgMgrSpec.getAgentAddress().getAgentContainer();
		this.orgMgrConfig = new AgentAddress(container.getContainerHost(), container.getContainerPort());
		
		// Get configuration of CtxQueryHandler and CtxCoord agents from OrgMgr, and CONSERT Engine from CtxCoord
		Future<Void> future = Future.future();
		
		final String findCtxCoordRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/";
		final String findCtxQueryHandlerRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.MANAGEMENT_ROUTE + "/find_query_handler/";
		final String findEngineRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.COORDINATION_ROUTE + "/find_engine/";
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		Future<Void> futureQueryHandler = Future.future();
		futureQueryHandler.setHandler(handler -> {
			
			// Query the OrgMgr agent to get the configuration to use for the CONSERT Engine
			client.get(ctxCoordConfig.getPort(), ctxCoordConfig.getIpAddress(), findEngineRoute,
					new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					
					resp.bodyHandler(new Handler<Buffer>() {

						@Override
						public void handle(Buffer buffer) {
							
							try {
								
								// Convert the statements to a Java object
								Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "",
										RDFFormat.TURTLE);
								convRepoConn.add(model);
								
								for(Statement s : model) {

									// Set the configuration
									if(s.getPredicate().stringValue().contains(rdfType)) {
										engineConfig = convManager.get(s.getSubject(), AgentAddress.class);
										break;
									}
								}
								
							} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
								System.err.println("Error while getting configuration for CtxQueryHandler: "
										+ e.getMessage());
								e.printStackTrace();
							}
							
							convRepoConn.clear();
							
							synchronized(future) {
								future.complete();
								future.notify();
							}
						}
					});
				}
				
			}).end();
		});
		
		Future<Void> futureCoord = Future.future();
		futureCoord.setHandler(handler -> {
			
			// Query the OrgMgr agent to get the configuration to use for the CtxQueryHandler
			client.get(orgMgrConfig.getPort(), orgMgrConfig.getIpAddress(), findCtxQueryHandlerRoute,
					new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					
					resp.bodyHandler(new Handler<Buffer>() {

						@Override
						public void handle(Buffer buffer) {
							
							try {
								
								// Convert the statements to a Java object
								Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "",
										RDFFormat.TURTLE);
								convRepoConn.add(model);
								
								for(Statement s : model) {

									// Set the configuration
									if(s.getPredicate().stringValue().contains(rdfType)) {
										ctxQueryHandlerConfig = convManager.get(s.getSubject(), AgentAddress.class);
										break;
									}
								}
								
							} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
								System.err.println("Error while getting configuration for CtxQueryHandler: "
										+ e.getMessage());
								e.printStackTrace();
							}
							
							convRepoConn.clear();
							
							futureQueryHandler.complete();
						}
					});
				}
				
			}).end();
		});
		
		// Query the OrgMgr agent to get the configuration to use for the CtxCoord
		client.get(orgMgrConfig.getPort(), orgMgrConfig.getIpAddress(), findCtxCoordRoute,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							// Convert the statements to a Java object
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							for(Statement s : model) {

								// Set the configuration
								if(s.getPredicate().stringValue().contains(rdfType)) {
									ctxCoordConfig = convManager.get(s.getSubject(), AgentAddress.class);
									break;
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while getting configuration for CtxCoord: " + e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.clear();
						
						futureCoord.complete();
					}
				});
			}
			
		}).end();

		// Wait for the configuration to be completely set before ending the method
		try {
			synchronized(future) {
				future.wait(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public String queryContext(String query) {

		StringBuilder result = new StringBuilder();
		Future<Void> future = Future.future();
		
		// Send the query
		this.queryContext(query, new Handler<Buffer>() {

			@Override
			public void handle(Buffer buffer) {
		
				result.append(buffer.toString());
				synchronized(future) {
					future.complete();
					future.notify();
				}
			}
		});
		
		// Wait for the result before returning it
		try {
			synchronized(future) {
				future.wait(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
		if (future.isComplete()) {
			return result.toString();
		}
		else {
			return null;
		}
	}
	
	@Override
	public void queryContext(String query, Handler<Buffer> handler) {
		
		// Send the query		
		this.client.get(engineConfig.getPort(), engineConfig.getIpAddress(), MiddlewareAPIImpl.ANSWER_QUERY_ROUTE,
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						resp.bodyHandler(handler);
					}
			
		}).end(query);
	}
	
	
	@Override
	public UUID subscribeContextUpdates(ContextSubscriptionRequest request) {
		
		StringBuilder uuid = new StringBuilder();
		Future<Void> future = Future.future();
		
		// Convert the ContextSubscriptionRequest Java object to RDF statements
		try {
			this.convManager.add(request);
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while converting context subscription request to RDF statements: "
					+ e.getMessage());
			e.printStackTrace();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();
		
		RepositoryResult<Statement> statements = this.convRepoConn.getStatements(null, null, null);
		
		while(statements.hasNext()) {
			writer.handleStatement(statements.next());
		}
		
		writer.endRDF();
		this.convRepoConn.clear();
		
		// Send the context subscription
		this.client.post(this.ctxQueryHandlerConfig.getPort(), this.ctxQueryHandlerConfig.getIpAddress(),
				MiddlewareAPIImpl.CONTEXT_SUBSCRIPTION_ROUTE, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						
						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								
								if(resp.statusCode() == 201) {
									uuid.append(buffer.toString());
								}
								
								synchronized(future) {
									future.complete();
									future.notify();
								}
							}
						});
					}
			
		}).putHeader("content-type", "text/turtle").end(baos.toString());
		
		// Wait for the result before returning it
		try {
			synchronized(future) {
				future.wait(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
		if (future.isComplete()) {
			return UUID.fromString(uuid.toString());
		}
		else {
			return null;
		}
	}
	
	@Override
	public List<AgentSpec> listProviders(URI ctxAssert) {
		
		List<AgentSpec> providers = new ArrayList<AgentSpec>();
		Future<Void> future = Future.future();
		
		// Get all the assertion capabilities
		
		this.client.get(this.ctxCoordConfig.getPort(), this.ctxCoordConfig.getIpAddress(),
				MiddlewareAPIImpl.ASSERTION_CAPABILITIES_ROUTE, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {

				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {

						try {
							
							// Convert the received RDF statements to Java objects
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "",
									RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							RepositoryResult<Statement> statements = convRepoConn
									.getStatements(null, MiddlewareAPIImpl.RDF_TYPE,
											MiddlewareAPIImpl.ASSERTION_CAPABILITY_IRI);
							
							while(statements.hasNext()) {
								
								Statement s = statements.next();
									
								AssertionCapability ac = convManager.get(s.getSubject(),
										AssertionCapability.class);
								
								// If the assertion capability is for the requested context assertion,
								// store its provider to return it at the end of the method
								if(ac.getContent().equals(ctxAssert)) {
									
									providers.add(ac.getProvider());
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while converting RDF statements to Java objects: "
									+ e.getMessage());
							e.printStackTrace();
						}
						
						synchronized(future) {
							future.complete();
							future.notify();
						}
					}
				});
			}
		}).end();
		
		// Wait for the result before returning it
		try {
			synchronized(future) {
				future.wait(10000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
		if (future.isComplete()) {
			return providers;
		}
		else {
			return null;
		}
	}
}
