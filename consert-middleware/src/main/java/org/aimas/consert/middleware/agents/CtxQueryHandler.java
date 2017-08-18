package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.config.QueryHandlerSpecification;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.protocol.RequestResource;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandler;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;
import org.eclipse.rdf4j.query.resultio.QueryResultParser;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONParser;
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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.Router;

/**
 * CtxQueryHandler agent implemented as a Vert.x server
 */
public class CtxQueryHandler extends AbstractVerticle implements Agent {

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where the agent is hosted

	private Repository dataRepo;  // repository containing the RDF data for queries
	private Repository subscriptionsRepo;  // repository containing the RDF data for context subscriptions

	public Map<UUID, ContextSubscription> contextSubscriptions; // list of context subscriptions
	public Map<UUID, RequestResource> ctxSubsResources; // list of resources for context subscriptions

	private AgentAddress ctxCoord;       // configuration to communicate with the CtxCoord agent
	private AgentAddress orgMgr;         // configuration to communicate with the OrgMgr agent
	private AgentAddress consertEngine;  // configuration to communicate with the CONSERT Engine
	
	private HttpClient client;  // client to use for the communications with the other agents

	private ScheduledExecutorService subscriptionsService;  // service that sends the queries for context subscriptions
	
	private Repository convRepo;  // repository used to convert Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository
	

	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		
		this.client = this.vertx.createHttpClient();

		// Initialization of the repositories
		this.dataRepo = new SailRepository(new MemoryStore());
		this.dataRepo.initialize();
		this.subscriptionsRepo = new SailRepository(new MemoryStore());
		this.subscriptionsRepo.initialize();
		
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);

		// Initialization of the lists
		this.contextSubscriptions = new HashMap<UUID, ContextSubscription>();
		this.ctxSubsResources = new HashMap<UUID, RequestResource>();

		// Get configuration
		Future<Void> futureConfig = Future.future();
		futureConfig.setHandler(handler -> {
			
			Future<Void> futureAgents = Future.future();
			futureAgents.setHandler(handlerAgents -> {

				// Create router
				RouteConfig routeConfig = new RouteConfigV1();
				this.router = routeConfig.createRouterDissemination(this.vertx, this);

				// Start server
				this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(),
						this.host, res -> {
							if (res.succeeded()) {
								System.out.println("Started CtxQueryHandler on port " + this.agentConfig.getPort()
									+ " host " + this.host);
							} else {
								System.out.println("Failed to start CtxQueryHandler server on port "
									+ this.agentConfig.getPort() + " host " + this.host);
							}

							future.complete();
						});
				
				this.subscriptionsService = Executors.newScheduledThreadPool(1);
				this.subscriptionsService.execute(new ContextSubscriptionTask());
			});
			
			this.findAgents(futureAgents);
		});
		
		// Get configuration of OrgMgr
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(QueryHandlerSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#CtxQueryHandlerSpec");
		
		if(orgMgrSpec != null) {
			CMMAgentContainer container = orgMgrSpec.getAgentAddress().getAgentContainer();
			this.orgMgr = new AgentAddress(container.getContainerHost(), container.getContainerPort());
		} else {
			// use a default value
			this.orgMgr = new AgentAddress("127.0.0.1", 8080);
		}

		this.host = "0.0.0.0";
		this.getConfigFromOrgMgr(futureConfig);
	}

	@Override
	public void stop() {		
		this.dataRepo.shutDown();
		this.subscriptionsRepo.shutDown();
		this.convRepoConn.close();
		this.convRepo.shutDown();
	}

	@Override
	public Repository getRepository() {
		return this.subscriptionsRepo;
	}
	
	
	private void findAgents(Future<Void> future) {
		
		final String findCtxCoordRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/";
		final String findEngineRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.COORDINATION_ROUTE + "/find_engine/";
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		Future<Void> futureCoord = Future.future();
		futureCoord.setHandler(handler -> {
			
			client.get(ctxCoord.getPort(), ctxCoord.getIpAddress(), findEngineRoute,
					new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					
					resp.bodyHandler(new Handler<Buffer>() {

						@Override
						public void handle(Buffer buffer) {
							
							try {
								
								Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "",
										RDFFormat.TURTLE);
								convRepoConn.add(model);
								
								for(Statement s : model) {

									if(s.getPredicate().stringValue().contains(rdfType)) {
										consertEngine = convManager.get(s.getSubject(), AgentAddress.class);
										break;
									}
								}
								
							} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
								System.err.println("Error while getting configuration for CtxQueryHandler: "
										+ e.getMessage());
								e.printStackTrace();
							}
							
							convRepoConn.clear();
							future.complete();
						}
					});
				}
				
			}).end();
		});
		
		this.client.get(orgMgr.getPort(), orgMgr.getIpAddress(), findCtxCoordRoute, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							for(Statement s : model) {

								if(s.getPredicate().stringValue().contains(rdfType)) {
									ctxCoord = convManager.get(s.getSubject(), AgentAddress.class);
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
	}
	
	
	private void getConfigFromOrgMgr(Future<Void> future) {

		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		final String registerRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE
				+ "/context_agents/";
		
		this.agentConfig = new AgentConfig();
		
		HttpClient client = this.vertx.createHttpClient();
		
		// Query the OrgMgr agent to get the configuration to use
		client.post(orgMgr.getPort(), orgMgr.getIpAddress(), registerRoute, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							// Convert the statements to an object
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							for(Statement s : model) {

								if(s.getPredicate().stringValue().contains(rdfType)) {
									AgentAddress addr = convManager.get(s.getSubject(), AgentAddress.class);
									agentConfig.setAddress(addr.getIpAddress());
									agentConfig.setPort(addr.getPort());
									break;
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while getting configuration for CtxQueryHandler: "
								+ e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.clear();
						future.complete();
					}
				});
			}
			
		}).putHeader("content-type", "text/plain").end("CtxQueryHandler");
	}
	
	
	public Repository getDataRepository() {
		return this.dataRepo;
	}

	public void addContextSubscription(UUID uuid, ContextSubscription cs, RequestResource res) {
		this.contextSubscriptions.put(uuid, cs);
		this.ctxSubsResources.put(uuid, res);
	}

	public ContextSubscription getContextSubscription(UUID uuid) {
		return this.contextSubscriptions.get(uuid);
	}

	public String getContextSubscriptionRDF(UUID uuid) {

		ContextSubscription ctxSubs = this.contextSubscriptions.get(uuid);

		// Connection to repository to get all the statements
		RepositoryConnection conn = this.subscriptionsRepo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		// Prepare to write RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();

		try {

			// Get all the statements corresponding to the given object (as the subject)
			Resource objRes = manager.getResource(ctxSubs.getId(), ContextSubscription.class);

			RepositoryResult<Statement> iter = conn.getStatements(objRes, null, null);

			// Write all the statements
			while (iter.hasNext()) {

				writer.handleStatement(iter.next());
			}

			conn.close();

		} catch (RepositoryException | RDFBeanException e) {

			conn.close();
			System.err.println("Error while getting information for object " + ctxSubs.getId());
			e.printStackTrace();
		}

		writer.endRDF();

		return writer.toString();
	}
	
	public ContextSubscription setContextSubscription(UUID uuid, ContextSubscription ctxSubs) {
		return this.contextSubscriptions.replace(uuid, ctxSubs);
	}

	public ContextSubscription removeContextSubscription(UUID uuid) {
		return this.contextSubscriptions.remove(uuid);
	}
	
	public RequestResource getResource(UUID uuid) {
		return this.ctxSubsResources.get(uuid);
	}

	public AgentConfig getAgentConfig() {
		return agentConfig;
	}

	public AgentAddress getEngineConfig() {
		return this.consertEngine;
	}
	
	
	// Sends the queries of context subscriptions every 5 seconds
	private class ContextSubscriptionTask implements Runnable {
		
		private static final String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
				+ RouteConfig.ENGINE_ROUTE + "/answer_query/";
		
		public void run() {
			
			for(Entry<UUID, ContextSubscription> entry : contextSubscriptions.entrySet()) {
				
				// Send the query to the engine
				client.get(consertEngine.getPort(), consertEngine.getIpAddress(),
						ContextSubscriptionTask.ANSWER_QUERY_ROUTE, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						
						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								
								// Parse the received JSON
								List<BindingSet> results = new ArrayList<BindingSet>();
								
								QueryResultParser parser = new SPARQLResultsJSONParser();
								parser.setQueryResultHandler(new QueryResultHandler() {

									@Override
									public void handleBoolean(boolean value) throws QueryResultHandlerException {}

									@Override
									public void handleLinks(List<String> linkUrls) throws QueryResultHandlerException {}

									@Override
									public void startQueryResult(List<String> bindingNames)
											throws TupleQueryResultHandlerException {}

									@Override
									public void endQueryResult() throws TupleQueryResultHandlerException {}

									@Override
									public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
										results.add(bindingSet);
									}
									
								});
								InputStream is = new ByteArrayInputStream(buffer.getBytes());
								
								try {
									parser.parseQueryResult(is);
									
								} catch (QueryResultParseException | QueryResultHandlerException | IOException e) {
									
									System.err.println("Error while parsing JSON query result: " + e.getMessage());
									e.printStackTrace();
								}
								

								// Update the resource and notify the subscriber only if the result has changed
								RequestResource resource = ctxSubsResources.get(entry.getKey());
								
								if(resource.hasResultChanged(results)) {
									
									resource.setResult(results);
									
									// Send notification to subscriber
									URI callbackURI = resource.getInitiatorCallbackURI();
									client.post(callbackURI.getPort(), callbackURI.getHost(), callbackURI.getPath(),
											new Handler<HttpClientResponse>() {

										@Override
										public void handle(HttpClientResponse response) {
										}
									}).end();
								}
							}
						});
					}
				}).putHeader("content-type", "text/turtle").end(entry.getValue().getSubscriptionQuery());
			}
			
			subscriptionsService.schedule(new ContextSubscriptionTask(), 5, TimeUnit.SECONDS);
		}
	}
}
