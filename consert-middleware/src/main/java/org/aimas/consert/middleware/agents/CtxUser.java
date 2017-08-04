package org.aimas.consert.middleware.agents;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.aimas.consert.middleware.model.AssertionUpdateMode;
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

/**
 * CtxUser agent implemented as a Vert.x server
 */
public class CtxUser extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties"; // path to the configuration file for this agent

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data

	private AgentConfig ctxCoord; // configuration to communicate with the CtxCoord agent
	private AgentConfig ctxQueryHandler; // configuration to communicate with the CtxQueryHandler agent
	private AgentConfig orgMgr; // configuration to communicate with the OrgMgr agent
	
	protected Map<URI, AssertionUpdateMode> updateModes;  // the update mode for all the enabled assertion types

	public static void main(String[] args) {

		// CtxUser.vertx.deployVerticle(CtxUser.class.getName());
	}

	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		this.updateModes = new HashMap<URI, AssertionUpdateMode>();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterUsage(vertx, this);

		// Read configuration
		try {

			Configuration config = new PropertiesConfiguration(CONFIG_FILE);

			this.agentConfig = AgentConfig.readCtxUserConfig(config);
			this.host = config.getString("CtxUser.host");

			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.ctxQueryHandler = AgentConfig.readCtxQueryHandlerConfig(config);
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
								"Started CtxUser on port " + this.agentConfig.getPort() + " host " + this.host);
					} else {
						System.out.println(
								"Failed to start CtxUser on port " + this.agentConfig.getPort() + " host " + this.host);
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
	
	public void startUpdates(URI assertionType, AssertionUpdateMode updateMode) {
		this.updateModes.put(assertionType, updateMode);
	}
	
	public void stopUpdates(URI assertionType) {
		this.updateModes.remove(assertionType);
	}
	
	public void alterUpdates(URI assertionType, AssertionUpdateMode newUpdateMode) {
		this.updateModes.replace(assertionType, newUpdateMode);
	}
	
	public AgentConfig getAgentConfig() {
		return this.agentConfig;
	}
}
