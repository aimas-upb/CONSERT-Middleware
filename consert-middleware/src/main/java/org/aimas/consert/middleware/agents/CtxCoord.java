package org.aimas.consert.middleware.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.model.AssertionCapability;
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

public class CtxCoord extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";  // path to the configuration file for this agent
	
	private static Vertx vertx = Vertx.vertx();  // Vertx instance
	private Router router;                       // router for communication with this agent
	
	private AgentConfig agentConfig;  // configuration values for this agent
	private String host;              // where this agent is hosted
	
	private Repository repo;  // repository containing the RDF data
	
	public Map<UUID, AssertionCapability> assertionCapabilities;  // list of assertion capabilities
	
	private AgentConfig ctxSensor;  // configuration to communicate with the CtxSensor agent
	private AgentConfig ctxUser;    // configuration to communicate with the CtxUser agent
	private AgentConfig orgMgr;     // configuration to communicate with the OrgMgr agent
	
	
	public static void main(String[] args) {
		
		CtxCoord.vertx.deployVerticle(CtxCoord.class.getName());		
	}
	
	@Override
	public void start(Future<Void> future) {
		
		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		
		// Initialization of the list
		this.assertionCapabilities = new HashMap<UUID, AssertionCapability>();
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterCoordination(vertx, this);
		
		// Read configuration
		try {
			
			Configuration config = new PropertiesConfiguration(CONFIG_FILE);
			
			this.agentConfig = AgentConfig.readCtxCoordConfig(config);
			this.host = config.getString("CtxCoord.host");
			
			this.ctxSensor = AgentConfig.readCtxSensorConfig(config);
			this.ctxUser = AgentConfig.readCtxUserConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);
			
		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		// Start server
		CtxCoord.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxCoord on port " + this.agentConfig.getPort() + " host " + this.host);
				} else {
					System.out.println("Failed to start CtxCoord on port " + this.agentConfig.getPort() + " host " +
						this.host);
				}
				
				future.complete();
			});
	}
	
	@Override
	public void stop() {
		this.repo.shutDown();
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
	
	public Repository getRepo() {
		return this.repo;
	}
}
