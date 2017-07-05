package org.aimas.consert.middleware.agents;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CtxSensor extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";  // path to the configuration file for this agent
	
	private Vertx vertx;  // Vertx instance
	private Router router;                       // router for communication with this agent
	
	private AgentConfig agentConfig;  // configuration values for this agent
	private String host;              // where this agent is hosted
	
	private AgentConfig ctxCoord;  // configuration to communicate with the CtxCoord agent
	private AgentConfig orgMgr;    // configuration to communicate with the OrgMgr agent 
	
	
	public static void main(String[] args) {
		
		//CtxSensor.vertx.deployVerticle(CtxSensor.class.getName());		
	}
	
	@Override
	public void start(Future<Void> future) {
		
		this.vertx = this.context.owner();
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterSensing(vertx, this);
		
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
		this.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxSensor on port " + this.agentConfig.getPort() + " host " +
						this.host);
				} else {
					System.out.println("Failed to start CtxSensor on port " + this.agentConfig.getPort() + " host " +
						this.host);
				}
				
				future.complete();
			});
	}
}
