package org.aimas.consert.middleware.agents;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CtxSensor extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";
	
	private static Vertx vertx = Vertx.vertx(); // Vertx instance
	private Router router;
	
	private AgentConfig agentConfig;
	private String host;
	
	private AgentConfig ctxCoord;
	private AgentConfig orgMgr;
	
	
	public static void main(String[] args) {
		
		CtxSensor.vertx.deployVerticle(CtxSensor.class.getName());		
	}
	
	@Override
	public void start() {
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterSensing(vertx);
		
		// Read configuration
		try {
			
			Configuration config = new PropertiesConfiguration(CONFIG_FILE);
			
			this.agentConfig = AgentConfig.readCtxSensorConfig(config);
			this.host = config.getString("CtxSensor.host");
			
			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);
			
		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		// Start server
		CtxSensor.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxSensor on port " + this.agentConfig.getPort() + " host " + this.host);
				} else {
					System.out.println("Failed to start CtxSensor on port " + this.agentConfig.getPort() + " host " +
						this.host);
				}
			});
	}
}
