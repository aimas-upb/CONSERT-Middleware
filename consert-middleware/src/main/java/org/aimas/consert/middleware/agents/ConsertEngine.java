package org.aimas.consert.middleware.agents;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aimas.consert.engine.EngineRunner;
import org.aimas.consert.engine.EventTracker;
import org.aimas.consert.engine.api.ContextAssertionListener;
import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.CoordinatorSpecification;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.utils.TestSetup;
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
import org.kie.api.runtime.KieSession;

/**
 * CONSERT Engine implemented as a Vert.x server
 */
public class ConsertEngine extends AbstractVerticle implements Agent, ContextAssertionListener {

	// route to use to ask for an update of the context subscriptions
	private final static String UPDATE_SUBSCRIPTIONS_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.DISSEMINATION_ROUTE + "/update_subscriptions/";
	
	// route to use to post a static context update
	private final static String STATIC_CONTEXT_UPDATE_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.COORDINATION_ROUTE + "/update_entity_descriptions/";
	
	// route to use to find the configuration of the CtxCoord agent
	private final static String FIND_CTXCOORD_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/";
	
	// route to use to find the configuration of the CtxQueryHandler agent
	private final static String FIND_CTXQUERYHANDLER_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
	+ RouteConfig.MANAGEMENT_ROUTE + "/find_query_handler/";
	
	protected Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data
	private RepositoryConnection repoConn;  // connection to the repository
	private RDFBeanManager manager;  // manager for the repository
	
	private AgentAddress orgMgr;  // configuration to communicate with the OrgMgr agent
	private AgentAddress ctxQueryHandler;  // configuration to communicate with the CtxQueryHandler agent
	private AgentAddress ctxCoord;  // configuration to communicate with the CtxCoord agent
	
	private HttpClient client;  // client to use for the communications with the other agents
	
	private Repository convRepo;  // repository used for the conversion between Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // the connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository
	
	private Thread engineRunner;  // thread to run the CONSERT Engine
	private EventTracker eventTracker;  // service that allows to add events in the engine
	private KieSession kSession;  // rules for the engine
	private ExecutorService insertionService;  // service allowing to insert context assertions in the CONSERT Engine
	
	
	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		
		this.client = this.vertx.createHttpClient();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		this.repoConn = this.repo.getConnection();
		this.manager = new RDFBeanManager(this.repoConn);
		
		// Initialization of the conversion repository
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterEngine(this.vertx, this);

		// Get configuration
		JsonObject config = this.config();
		this.agentConfig = new AgentConfig(config.getString("address"), config.getInteger("port"));
		this.host = config.getString("host");
		
		// Get configuration of OrgMgr
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(CoordinatorSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#CtxCoordSpec");
		
		if(orgMgrSpec != null) {
			CMMAgentContainer container = orgMgrSpec.getAgentAddress().getAgentContainer();
			this.orgMgr = new AgentAddress(container.getContainerHost(), container.getContainerPort());
		} else {
			// use a default value
			this.orgMgr = new AgentAddress("127.0.0.1", 8080);
		}

		// Start server
		this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println("Started CONSERT Engine on port " + this.agentConfig.getPort() + " host "
							+ this.host);
					} else {
						System.out.println("Failed to start CONSERT Engine on port " + this.agentConfig.getPort()
							+ " host " + this.host);
					}					

					// Start CONSERT Engine
					this.kSession = TestSetup.getKieSessionFromResources("rules/HLA.drl");
			    	this.engineRunner = new Thread(new EngineRunner(kSession));
			    	this.eventTracker = new EventTracker(kSession);
			    	this.eventTracker.addEventListener(this);
			    	this.insertionService = Executors.newSingleThreadExecutor();
			    	
			    	this.engineRunner.start();
			    	
			    	// get the configuration of the CtxCoord agent to communicate with it
			    	this.findCoordinator(future);
				});
	}

	@Override
	public void stop() {
		
		//dumpRepository("dump.txt");
		
		// Close the repositories
		this.repoConn.close();
		this.repo.shutDown();
		
		this.convRepoConn.close();
		this.convRepo.shutDown();

		// Export the data to a HTML page displaying a graph, and close all the services
    	// PlotlyExporter.exportToHTML(null, this.kSession);
    	this.insertionService.shutdownNow();
    	this.kSession.halt();
    	this.kSession.dispose();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}

	@Override
	public void notifyAssertionInserted(ContextAssertion assertion) {
		try {
			// Insert the new assertion in the repository, and notify that the data has been updated
			manager.add(assertion);
			this.notifyAssertionUpdate();
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while inserting assertion in engine's repository: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void notifyAssertionDeleted(ContextAssertion assertion) {
		try {
			// Insert the assertion from the repository, and notify that the data has been updated
			manager.delete(assertion.getAssertionIdentifier(), ContextAssertion.class);
			this.notifyAssertionUpdate();
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while deleting assertion from engine's repository: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Allows to find the configuration of the CtxCoord agent in asynchronous mode
	 * @param future contains the handler to execute when once the configuration has been received
	 */
	private void findCoordinator(Future<Void> future) {
		
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		// ask for the CtxCoord configuration to the OrgMgr agent
		this.client.get(this.orgMgr.getPort(), this.orgMgr.getIpAddress(), ConsertEngine.FIND_CTXCOORD_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							// parse the response and set the configuration value
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
						future.complete();
					}
				});
			}
			
		}).end();
	}
	
	/**
	 * Allows to find the configuration of the CtxQueryHandler agent in asynchronous mode
	 * @param future contains the handler to execute when once the configuration has been received
	 */
	public void findQueryHandler(Future<Void> future) {
		
		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		this.client.get(this.orgMgr.getPort(), this.orgMgr.getIpAddress(), ConsertEngine.FIND_CTXQUERYHANDLER_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							// parse the response and set the configuration value
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							for(Statement s : model) {

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
	}
	
	/**
	 * Notifies the CtxQueryHandler and CtxCoord agents that the assertions have been updated
	 */
	private void notifyAssertionUpdate() {
		
		// Notify the CtxQueryHandler agent if there is one
		if(this.ctxQueryHandler != null) {
			
			this.client.put(this.ctxQueryHandler.getPort(), this.ctxQueryHandler.getIpAddress(),
					ConsertEngine.UPDATE_SUBSCRIPTIONS_ROUTE, new Handler<HttpClientResponse>() {
	
						@Override
						public void handle(HttpClientResponse event) {
						}
			}).end();
		}
			
		// Notify the CtxCoord agent
		this.client.put(this.ctxCoord.getPort(), this.ctxCoord.getIpAddress(),
				ConsertEngine.STATIC_CONTEXT_UPDATE_ROUTE, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse event) {
					}
		}).end();
	}
	
	/**
	 * Inserts a context assertion in the engine
	 * @param ca the context assertion to insert
	 */
	public void insertEvent(ContextAssertion ca) {
		this.insertionService.execute(new EventInsertionTask(ca));
	}
	
	/**
	 * Allows to insert an event in the engine
	 */
	private class EventInsertionTask implements Runnable {
		private ContextAssertion ca;
		
		EventInsertionTask(ContextAssertion ca) {
			this.ca = ca;
		}
		
		public void run() {
			eventTracker.insertAtomicEvent(ca);
        }
	}
	
	public AgentAddress getCtxQueryHandler() {		
		return this.ctxQueryHandler;
	}
	
	/**
	 * Write the content of the repository to a file (for test purposes)
	 * @param filename name of the file to write
	 */
	private void dumpRepository(String filename) {
		
		// Write only the statements that concern the HLAs
		List<IRI> iris = new ArrayList<IRI>();
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/WorkingHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/ExerciseHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/DiscussingHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/DiningHLA"));
		
		try {
			// Write the file
			Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, fileWriter);
			
			writer.startRDF();
			
			for(IRI iri : iris) {
				RepositoryResult<Statement> statements = this.repoConn.getStatements(null, null, iri);
				while(statements.hasNext()) {
					writer.handleStatement(statements.next());
				}
			}
			
			writer.endRDF();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
