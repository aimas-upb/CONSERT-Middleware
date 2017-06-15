package org.aimas.consert.middleware.agents;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CtxQueryHandler extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";
	
	private static Vertx vertx = Vertx.vertx(); // Vertx instance
	private Router router;
	
	private AgentConfig agentConfig;
	private String host;
	
	private AgentConfig ctxCoord;
	private AgentConfig orgMgr;
	
	
	public static void main(String[] args) {
		
		CtxQueryHandler.vertx.deployVerticle(CtxQueryHandler.class.getName());		
	}
	
	@Override
	public void start() {
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterDissemination(vertx);
		
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
		CtxQueryHandler.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxQueryHandler on port " + this.agentConfig.getPort() + " host " +
						this.host);
				} else {
					System.out.println("Failed to CtxQueryHandler server on port " + this.agentConfig.getPort() +
						" host " + this.host);
				}
			});
	}
}
