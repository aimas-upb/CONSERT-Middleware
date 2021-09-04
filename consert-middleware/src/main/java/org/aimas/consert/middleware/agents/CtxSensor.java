package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.middleware.model.tasking.UpdateModeState;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.Constants;
import org.aimas.consert.model.content.ContextAssertion;
import org.apache.tools.ant.taskdefs.Sleep;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * Base class for a CtxSensor agent implemented as a Vert.x server
 */
public abstract class CtxSensor extends AbstractVerticle implements Agent {
	
	// route to use to insert a new context assertion
	private static final String INSERT_CONTEXT_ASSERTION_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.COORDINATION_ROUTE + "/insert_context_assertion/";
	
	// route to use to find the CtxCoord agent
	private static final String FIND_CTXCOORD_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/";
	
	// route to use to register an agent to the OrgMgr
	private static final String REGISTER_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/context_agents/";
	

	private Vertx vertx;  // Vertx instance
	private Router router;  // router for communication with this agent

	protected AgentConfig agentConfig;  // configuration values for this agent
	private String host;  // where this agent is hosted
	
	private boolean isFinished = false;  // allows to know when the CtxSensor has finished sending all the events

	private Repository repo;  // repository containing the RDF data

	private AgentAddress ctxCoord;  // configuration to communicate with the CtxCoord agent
	protected AgentAddress orgMgr;  // configuration to communicate with the OrgMgr agent
	
	private HttpClient client;  // an HTTP client that can be used to send requests
	
	private List<UUID> assertionCapabilitiesIds;  // identifiers of the sent assertion capabilities
	
	protected Map<URI, UpdateModeState> updateModes;  // the update mode for all the assertion types and their state
	
	private Repository convRepo;  // repository used to convert Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository


	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		this.client = this.vertx.createHttpClient();
		this.updateModes = new HashMap<URI, UpdateModeState>();

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
		this.router = routeConfig.createRouterSensing(vertx, this);

		// Get configuration
		Future<Void> futureConfig = Future.future();
		futureConfig.setHandler(handler -> {
			
			Future<Void> futureAgents = Future.future();
			futureAgents.setHandler(handlerAgents -> {
				
				// Start server
				HttpServer server = this.vertx.createHttpServer();
				server.requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host, res -> {
					if (res.succeeded()) {
						System.out.println("Started CtxSensor on port " + server.actualPort() + " host " + this.host);
					} else {
						System.out.println("Failed to start CtxSensor on port " + server.actualPort() + " host "
								+ this.host);
					}
		
					// Send the assertion capabilities before doing anything
//					this.assertionCapabilitiesIds = new LinkedList<UUID>();
//					this.sendAssertionCapabilities(future);
					
					// Read events from the adaptor and send them to the CtxCoord agent
//					this.readEvents();
				});
			});
			
			this.findAgents(futureAgents);
		});
		this.host = "127.0.0.1";
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
	 * Allows to find the configuration of the required agents: CtxCoord
	 * @param future contains the handler to execute once the configurations have been received
	 */
	private void findAgents(Future<Void> future) {
		
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		this.client.get(this.orgMgr.getPort(), this.orgMgr.getIpAddress(), CtxSensor.FIND_CTXCOORD_ROUTE,
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

								if(s.getPredicate().stringValue().contains(rdfType)) {
									
									// Set the configuration
									ctxCoord = convManager.get(s.getSubject(), AgentAddress.class);
									break;
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while getting configuration for CtxCoord: " + e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.clear();
						future.complete();
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
		
		HttpClient client = this.vertx.createHttpClient();
		
		// Query the OrgMgr agent to get the configuration to use
		client.post(this.orgMgr.getPort(), this.orgMgr.getIpAddress(), CtxSensor.REGISTER_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
//							System.out.println(buffer);
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
							System.err.println("Error while getting configuration for CtxSensor: " + e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.clear();
						
						future.complete();
					}
				});
			}
			
		}).putHeader("content-type", "text/plain").end("CtxSensor");
	}
	
	
	/**
	 * starts reading the events from the adaptor and sending them to the CtxCoord
	 */
	protected abstract void readEvents();

	/**
	 * send default assertion capability
	 * @param future allows the execution in async mode
	 */
	protected abstract void sendAssertionCapabilities(Future<Void> future);
	
	/**
	 * send the assertion capability to the CtxCoord
	 * @param acs the assertion capabilities to send
	 * @param future allows the execution in async mode
	 */
	protected void sendAssertionCapabilities(List<AssertionCapability> acs, Future<Void> future) {
		
		String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
				"/context_assertions/";
		
		// Convert the AssertionCapability objects to RDF statements		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			
			// Convert each AssertionCapability one by one
			
			for(AssertionCapability ac : acs) {
			
				baos.reset();
				RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
				writer.startRDF();
				
				this.convRepoConn.clear();
				this.convManager.add(ac);
				
				RepositoryResult<Statement> iter = this.convRepoConn.getStatements(null, null, null);
				
				while(iter.hasNext()) {
					writer.handleStatement(iter.next());
				}
				
				writer.endRDF();
				
				// send to the CtxCoord
				this.client.post(ctxCoord.getPort(), ctxCoord.getIpAddress(), route, new Handler<HttpClientResponse>() {
	
					@Override
					public void handle(HttpClientResponse resp) {
						
						if(resp.statusCode() == 201) {
							resp.bodyHandler(new Handler<Buffer>() {
	
								@Override
								public void handle(Buffer buffer) {
									assertionCapabilitiesIds.add(UUID.fromString(buffer.toString()));
									
									if(assertionCapabilitiesIds.size() == acs.size()) {
										future.complete();
									}
								}
	
							});
						}
					}
					
				}).putHeader("content-type", "text/turtle").end(baos.toString());
			}
			
		} catch (RepositoryException | RDFBeanException e) {
			e.printStackTrace();
		}
		
		this.convRepoConn.clear();
	}
	
	/**
	 *  Delete all the assertion capabilities from this CtxSensor on the CtxCoord
	 */
	protected void deleteAssertionCapabilities() {
		
		String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
				"/context_assertions/";
		
		// send to the CtxCoord
		for(UUID uuid : this.assertionCapabilitiesIds) {
			
			this.client.delete(ctxCoord.getPort(), ctxCoord.getIpAddress(), route + uuid.toString() + "/",
					new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					
					if(resp.statusCode() != 200) {
						System.err.println("Error while deleting assertion capability " + uuid.toString());
					}
				}
				
			}).end();
		}
		
		this.assertionCapabilitiesIds.clear();
	}
	
	/**
	 *  Sends the event to the known CtxCoord
	 * @param event the context assertion to send
	 */
	protected void sendEvent(ContextAssertion event) {
		
		// First, we need to convert the objects to RDF statements
		// We use the repository for this
		RepositoryConnection conn = this.repo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);
		
		try {
			manager.add(event);
		} catch (RepositoryException | RDFBeanException e) {
			e.printStackTrace();
		}
		
		// Prepare to write the RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, baos);
		writer.startRDF();

		List<Statement> bindingStatements = new LinkedList<Statement>();
		List<Statement> assertionStatements = new LinkedList<Statement>();
		List<Statement> annotationStatements = new LinkedList<Statement>();
		RepositoryResult<Statement> iter = conn.getStatements(null, null, null);
		
		// Separation of statements for binding classes and for assertions
		while(iter.hasNext()) {
			Statement s = iter.next();
			
			if(s.getPredicate().stringValue().contains("bindingClass")) {
				bindingStatements.add(s);
			} else if(s.getPredicate().stringValue().contains(Constants.ANNOTATION_NS)
					|| s.getObject().stringValue().contains(Constants.ANNOTATION_NS)) {
				annotationStatements.add(s);
			} else {
				assertionStatements.add(s);
			}
		}
		
		conn.clear();
		Resource assertG = SimpleValueFactory.getInstance()
				.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#assertionGraph");
		Resource annG = SimpleValueFactory.getInstance()
				.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#annotationGraph");
		conn.add(bindingStatements);
		conn.add(assertionStatements, assertG);
		conn.add(annotationStatements, annG);
		
		// Write all the graphs
		iter = conn.getStatements(null, null, null);
		while(iter.hasNext()) {
			writer.handleStatement(iter.next());
		}
		
		// Clean
		writer.endRDF();
		conn.clear();
		conn.close();

		// submit insertion task
		this.client.post(this.ctxCoord.getPort(), this.ctxCoord.getIpAddress(), this.INSERT_CONTEXT_ASSERTION_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				if(resp.statusCode() != 201) {
					System.err.println("CtxCoord returned error while inserting context assertion");
				}
			}
		}).end(baos.toString());
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	protected void setFinished(boolean finished) {
		isFinished = finished;
	}
	
	/**
	 * Asks the CtxSensor to start sending updates
	 * @param assertionType the type of assertions updates to send
	 * @param updateMode the mode to use for the updates
	 */
	public void startUpdates(URI assertionType, AssertionUpdateMode updateMode) {
		
		UpdateModeState modeState = this.updateModes.get(assertionType);
		
		if(modeState == null) {
			this.updateModes.put(assertionType, new UpdateModeState(updateMode, true));
		} else {
			modeState.setEnabled(true);
		}
	}
	
	/**
	 * Asks the CtxSensor to stop sending updates
	 * @param assertionType the type of assertions updates to send
	 */
	public void stopUpdates(URI assertionType) {
		UpdateModeState modeState = this.updateModes.get(assertionType);
		modeState.setEnabled(false);
	}
	
	/**
	 * Asks the CtxSensor to change an update mode
	 * @param assertionType the type of assertions updates to change
	 * @param newUpdateMode the new mode to use for the updates
	 */
	public void alterUpdates(URI assertionType, AssertionUpdateMode newUpdateMode) {
		
		UpdateModeState modeState = this.updateModes.get(assertionType);
		modeState.setUpdateMode(newUpdateMode);
	}
	
	public AgentConfig getAgentConfig() {
		return this.agentConfig;
	}
}
