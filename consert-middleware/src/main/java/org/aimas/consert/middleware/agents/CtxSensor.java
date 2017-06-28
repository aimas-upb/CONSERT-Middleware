package org.aimas.consert.middleware.agents;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.Router;

public class CtxSensor extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";  // path to the configuration file for this agent
	
	private static Vertx vertx = Vertx.vertx();  // Vertx instance
	private Router router;                       // router for communication with this agent
	
	private AgentConfig agentConfig;  // configuration values for this agent
	private String host;              // where this agent is hosted
	
	private AgentConfig ctxCoord;  // configuration to communicate with the CtxCoord agent
	private AgentConfig orgMgr;    // configuration to communicate with the OrgMgr agent 
	
	
	public static void main(String[] args) {
		
		CtxSensor.vertx.deployVerticle(CtxSensor.class.getName());		
	}
	
	@Override
	public void start() {
		
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
		CtxSensor.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxSensor on port " + this.agentConfig.getPort() + " host " +
						this.host);
				} else {
					System.out.println("Failed to start CtxSensor on port " + this.agentConfig.getPort() + " host " +
						this.host);
				}
			});
		
		
		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#> .\n"
				+ "@prefix assertion-capability: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapability/> .\n"
				+ "@prefix context-annotation: <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#ContextAnnotation/> .\n"
				+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/> .\n\n"
				+ "assertion-capability:foo a protocol:AssertionCapability ;\n"
				+ "    protocol:hasContent context-annotation:ann1 ;\n"
				+ "    protocol:hasProvider agent-spec:CtxSensor .\n"
				+ "context-annotation:ann1 a annotation:TimestampAnnotation .\n"
				+ "agent-spec:CtxSensor a protocol:AgentSpec ;\n"
				+ "    protocol:hasAddress agent-address:CtxSensorAddress ;\n"
				+ "    protocol:hasIdentifier \"CtxSensor\" .\n"
				+ "agent-address:CtxSensorAddress a protocol:AgentAddress ;\n"
				+ "    protocol:ipAddress \"127.0.0.1\" ;\n"
				+ "    protocol:port 8080 .\n";
		
		CtxSensor.vertx.createHttpClient()
			.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(), "/api/v1/coordination/context_assertions/", new Handler<HttpClientResponse>() {
				
					@Override
					public void handle(HttpClientResponse resp) {
						
						resp.bodyHandler(new Handler<Buffer>() {
							
							@Override
							public void handle(Buffer buffer) {
								
								System.out.println("CtxCoord answered: " + buffer.toString());
							}
						});
					}
				})
			.putHeader("content-type", "text/plain")
			.end(rdfData);
	}
}
