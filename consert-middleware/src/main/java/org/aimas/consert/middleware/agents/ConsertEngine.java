package org.aimas.consert.middleware.agents;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aimas.consert.engine.EngineRunner;
import org.aimas.consert.engine.EventTracker;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.TestSetup;
import org.aimas.consert.utils.PlotlyExporter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * CONSERT Engine implemented as a Vert.x server
 */
public class ConsertEngine extends AbstractVerticle implements Agent {

	protected Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data
	
	private Thread engineRunner;               // thread to run the CONSERT Engine
	private EventTracker eventTracker;         // service that allows to add events in the engine
	private KieSession kSession;               // rules for the engine
	private ExecutorService insertionService;  // service allowing to insert context assertions in the CONSERT Engine
	
	
	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repositories
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterEngine(this.vertx, this);

		// Get configuration
		JsonObject config = this.config();
		this.agentConfig = new AgentConfig(config.getString("address"), config.getInteger("port"));
		this.host = config.getString("host");

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
			    	this.insertionService = Executors.newSingleThreadExecutor();
			    	
			    	this.engineRunner.start();
					future.complete();
				});
	}
	
	public void stopVertx() {
		this.vertx.close();
	}

	@Override
	public void stop() {
		this.repo.shutDown();

    	PlotlyExporter.exportToHTML(null, this.kSession);
    	this.insertionService.shutdownNow();
    	this.kSession.halt();
    	this.kSession.dispose();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}
	
	public void insertEvent(ContextAssertion ca) {
		this.insertionService.execute(new EventInsertionTask(ca));
	}
	
	private class EventInsertionTask implements Runnable {
		private ContextAssertion ca;
		
		EventInsertionTask(ContextAssertion ca) {
			this.ca = ca;
		}
		
		public void run() {
			eventTracker.insertAtomicEvent(ca);
        }
	}
}
