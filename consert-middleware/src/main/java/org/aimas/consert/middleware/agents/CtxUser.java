package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.config.UserSpecification;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
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
 * CtxUser agent implemented as a Vert.x server
 */
public class CtxUser extends AbstractVerticle implements Agent {
	
	// route to use to find the CtxCoord agent
	private static final String FIND_CTXCOORD_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/";
	
	// route to use to find the CtxQueryHandler agent
	private static final String FIND_CTXQUERYHANDLER_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/find_query_handler/";
	
	// route to use to register an agent to the OrgMgr
	private final static String REGISTER_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/context_agents/";
	

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data

	private AgentAddress ctxCoord; // configuration to communicate with the CtxCoord agent
	private AgentAddress ctxQueryHandler; // configuration to communicate with the CtxQueryHandler agent
	private AgentAddress orgMgr; // configuration to communicate with the OrgMgr agent
	
	HttpClient client;  // client to use for the communications with the other agents
	
	protected Map<URI, AssertionUpdateMode> updateModes;  // the update mode for all the enabled assertion types
	
	private Repository convRepo;  // repository used to convert Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository


	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		this.updateModes = new HashMap<URI, AssertionUpdateMode>();
		
		this.client = this.vertx.createHttpClient();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		
		// Initialization of the conversion repository
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterUsage(vertx, this);

		// Get configuration
		Future<Void> futureConfig = Future.future();
		futureConfig.setHandler(handler -> {
			
			Future<Void> futureAgents = Future.future();
			futureAgents.setHandler(handlerAgents -> {

				// Start server
				this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(),
						this.host, res -> {
							if (res.succeeded()) {
								System.out.println("Started CtxUser on port " + this.agentConfig.getPort()
									+ " host " + this.host);
							} else {
								System.out.println("Failed to start CtxUser on port " + this.agentConfig.getPort()
									+ " host " + this.host);
							}

							future.complete();
						});
			});
			
			this.findAgents(futureAgents);
		});
		
		// Get configuration of OrgMgr
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(UserSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#CtxUserSpec");
		
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
		this.repo.shutDown();
		this.convRepoConn.close();
		this.convRepo.shutDown();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}
	
	
	/**
	 * Allows to find the configuration of the required agents: CtxCoord and CtxQueryHandler
	 * @param future contains the handler to execute once the configurations have been received
	 */
	private void findAgents(Future<Void> future) {
		
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		Future<Void> futureCoord = Future.future();
		futureCoord.setHandler(handler -> {
			
			// Query the OrgMgr agent to get the configuration to use for the CtxQueryHandler
			client.get(orgMgr.getPort(), orgMgr.getIpAddress(), CtxUser.FIND_CTXQUERYHANDLER_ROUTE,
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
										ctxQueryHandler = convManager.get(s.getSubject(), AgentAddress.class);
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
		
		// Query the OrgMgr agent to get the configuration to use for the CtxCoord
		client.get(this.orgMgr.getPort(), this.orgMgr.getIpAddress(), CtxUser.FIND_CTXCOORD_ROUTE,
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
	
	
	/**
	 * Get the configuration to use from the OrgMgr agent in asynchronous mode
	 * @param future contains the handler to execute when once the configuration has been received
	 */
	private void getConfigFromOrgMgr(Future<Void> future) {

		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		this.agentConfig = new AgentConfig();
		
		// Query the OrgMgr agent to get the configuration to use
		client.post(orgMgr.getPort(), orgMgr.getIpAddress(), CtxUser.REGISTER_ROUTE, new Handler<HttpClientResponse>() {

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

								if(s.getPredicate().stringValue().contains(rdfType)) {
									
									// Set the configuration
									AgentAddress addr = convManager.get(s.getSubject(), AgentAddress.class);
									agentConfig.setAddress(addr.getIpAddress());
									agentConfig.setPort(addr.getPort());
									break;
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while getting configuration for CtxUser: " + e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.clear();
						
						future.complete();
					}
				});
			}
			
		}).putHeader("content-type", "text/plain").end("CtxUser");
	}
	
	
	/**
	 * Asks the CtxUser to start sending updates
	 * @param assertionType the type of assertions updates to send
	 * @param updateMode the mode to use for the updates
	 */
	public void startUpdates(URI assertionType, AssertionUpdateMode updateMode) {
		this.updateModes.put(assertionType, updateMode);
	}
	
	/**
	 * Asks the CtxUser to stop sending updates
	 * @param assertionType the type of assertions updates to send
	 */
	public void stopUpdates(URI assertionType) {
		this.updateModes.remove(assertionType);
	}
	
	/**
	 * Asks the CtxUser to change an update mode
	 * @param assertionType the type of assertions updates to change
	 * @param newUpdateMode the new mode to use for the updates
	 */
	public void alterUpdates(URI assertionType, AssertionUpdateMode newUpdateMode) {
		this.updateModes.replace(assertionType, newUpdateMode);
	}
	
	public AgentConfig getAgentConfig() {
		return this.agentConfig;
	}
	
	public AgentAddress getCtxQueryHandlerConfig() {
		return this.ctxQueryHandler;
	}
}
