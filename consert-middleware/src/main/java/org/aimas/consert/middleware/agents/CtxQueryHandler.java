package org.aimas.consert.middleware.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CtxQueryHandler extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties";  // path to the configuration file for this agent
	
	private Vertx vertx;    // Vertx instance
	private Router router;  // router for communication with this agent
	
	private AgentConfig agentConfig;  // configuration values for this agent
	private String host;              // where the agent is hosted

	private Repository repo;  // repository containing the RDF data
	
	public Map<UUID, ContextSubscription> contextSubscriptions;  // list of context subscriptions
	
	private AgentConfig ctxCoord;  // configuration to communicate with the CtxCoord agent
	private AgentConfig orgMgr;    // configuration to communicate with the OrgMgr agent
	
	
	public static void main(String[] args) {
		
		//CtxQueryHandler.vertx.deployVerticle(CtxQueryHandler.class.getName());		
	}
	
	@Override
	public void start(Future<Void> future) {
		
		this.vertx = this.context.owner();
		
		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		
		// Initialization of the lists
		this.contextSubscriptions = new HashMap<UUID, ContextSubscription>();
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterDissemination(this.vertx, this);
		
		// Read configuration
		try {
			
			Configuration config = new PropertiesConfiguration(CONFIG_FILE);
			
			this.agentConfig = AgentConfig.readCtxQueryHandlerConfig(config);
			this.host = config.getString("CtxQueryHandler.host");
			
			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);
			
		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		// Start server
		this.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxQueryHandler on port " + this.agentConfig.getPort() + " host " +
						this.host);
				} else {
					System.out.println("Failed to start CtxQueryHandler server on port " + this.agentConfig.getPort() +
						" host " + this.host);
				}
				
				future.complete();
			});
	}
	
	@Override
	public void stop() {
		this.repo.shutDown();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}

	public void addContextSubscription(UUID key, ContextSubscription cs) {
		this.contextSubscriptions.put(key, cs);
	}
}
