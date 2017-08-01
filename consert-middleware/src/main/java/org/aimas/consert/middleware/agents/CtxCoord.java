package org.aimas.consert.middleware.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aimas.consert.engine.EngineRunner;
import org.aimas.consert.engine.EventTracker;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionCapabilitySubscription;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.TestSetup;
import org.aimas.consert.utils.PlotlyExporter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * CtxCoord agent implemented as a Vert.x server
 */
public class CtxCoord extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties"; // path to the
															// configuration
															// file for this
															// agent

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data

	public Map<UUID, AssertionCapability> assertionCapabilities; // list of
																	// assertion
																	// capabilities
	public Map<UUID, AssertionCapabilitySubscription> assertionCapabilitySubscriptions; // list
																						// of
																						// subscriptions
																						// for
																						// assertion
																						// capabilities

	private List<AgentConfig> ctxSensors;  // configuration to communicate with the CtxSensor agents
	private AgentConfig ctxUser;           // configuration to communicate with the CtxUser agent
	private AgentConfig orgMgr;            // configuration to communicate with the OrgMgr agent
	
	private Thread engineRunner;        // thread to run the CONSERT Engine
	private EventTracker eventTracker;  // service that allows to add events in the engine
	private KieSession kSession;        // rules for the engine
	private ExecutorService insertionService;

	public static void main(String[] args) {

		// CtxCoord.vertx.deployVerticle(CtxCoord.class.getName());
	}

	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Initialization of the lists
		this.assertionCapabilities = new HashMap<UUID, AssertionCapability>();
		this.assertionCapabilitySubscriptions = new HashMap<UUID, AssertionCapabilitySubscription>();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterCoordination(this.vertx, this);

		// Read configuration
		try {

			Configuration config = new PropertiesConfiguration(CONFIG_FILE);

			this.agentConfig = AgentConfig.readCtxCoordConfig(config);
			this.host = config.getString("CtxCoord.host");

			this.ctxSensors = AgentConfig.readCtxSensorConfig(config);
			this.ctxUser = AgentConfig.readCtxUserConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);

		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}

		// Start server
		this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println(
								"Started CtxCoord on port " + this.agentConfig.getPort() + " host " + this.host);
					} else {
						System.out.println("Failed to start CtxCoord on port " + this.agentConfig.getPort() + " host "
								+ this.host);
					}

					future.complete();
				});
		
		
		// Start CONSERT Engine
		this.kSession = TestSetup.getKieSessionFromResources("rules/HLA.drl");
    	this.engineRunner = new Thread(new EngineRunner(kSession));
    	this.eventTracker = new EventTracker(kSession);
    	this.insertionService = Executors.newSingleThreadExecutor();
    	
    	this.engineRunner.start();
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

	public AssertionCapability addAssertionCapability(UUID uuid, AssertionCapability ac) {
		return this.assertionCapabilities.put(uuid, ac);
	}

	public AssertionCapability getAssertionCapability(UUID uuid) {
		return this.assertionCapabilities.get(uuid);
	}

	public Collection<AssertionCapability> getAssertionCapabilitiesValues() {
		return this.assertionCapabilities.values();
	}

	public AssertionCapability removeAssertionCapability(UUID uuid) {
		return this.assertionCapabilities.remove(uuid);
	}

	public AssertionCapabilitySubscription addAssertionCapabilitySubscription(UUID uuid,
			AssertionCapabilitySubscription acs) {
		return this.assertionCapabilitySubscriptions.put(uuid, acs);
	}

	public AssertionCapabilitySubscription getAssertionCapabilitySubscription(UUID uuid) {
		return this.assertionCapabilitySubscriptions.get(uuid);
	}

	public AssertionCapabilitySubscription removeAssertionCapabilitySubscription(UUID uuid) {
		return this.assertionCapabilitySubscriptions.remove(uuid);
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
