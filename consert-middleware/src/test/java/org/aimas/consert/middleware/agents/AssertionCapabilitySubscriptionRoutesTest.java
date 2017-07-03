package org.aimas.consert.middleware.agents;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for AssertionCapability routes
 */
@RunWith(VertxUnitRunner.class)
public class AssertionCapabilitySubscriptionRoutesTest
{
	private final String CONFIG_FILE = "agents.properties";
	private final String postQuery = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
			+ "@prefix assertion-capability-subscription: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapabilitySubscription/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			+ "protocol:AssertionCapabilitySubscription rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionCapabilitySubscription\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n\n"
			+ "assertion-capability-subscription:foo a protocol:AssertionCapabilitySubscription ;\n"
			+ "    protocol:hasCapabilityQuery \"the capability query\"^^xsd:string;\n"
			+ "    protocol:hasSubscriber agent-spec:CtxUser .\n"
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser1\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8081\"^^xsd:int .\n";
			
	private Vertx vertx;
	private AgentConfig ctxCoord;
	private String resourceUUID;
	private HttpClient httpClient;
	
	
	@Before
	public void setUp(TestContext context) throws IOException, ConfigurationException {
		
    	// Read configuration files
		Configuration config;
		
		config = new PropertiesConfiguration(CONFIG_FILE);
		this.ctxCoord = AgentConfig.readCtxCoordConfig(config);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		this.vertx.deployVerticle(CtxCoord.class.getName(), context.asyncAssertSuccess());
		
		this.httpClient = this.vertx.createHttpClient();
	}
	
    @After
    public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
    }
    
    
    @Test
    public void testPost(TestContext context) {
    	
		Async async = context.async();
		this.post(context, async);
		async.await();
    }
    
    public void post(TestContext context, Async async) {
    	
    	// POST: insert data that we will try to fetch in the test methods
		
		this.httpClient
			.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
					"/api/v1/coordination/assertion_capability_subscriptions/", new Handler<HttpClientResponse>() {
				
			@Override
			public void handle(HttpClientResponse resp) {
				
				if(resp.statusCode() != 201) {
					context.fail("Failed to create AssertionCapabilitySubscription, code " + resp.statusCode());
					async.complete();
				} else {
					
					// Get the created resource's UUID to make requests on it later
					resp.bodyHandler(new Handler<Buffer>() {

						@Override
						public void handle(Buffer buffer) {
							resourceUUID = buffer.toString();
							async.complete();
						}
						
					});
				}
			}
		})
		.putHeader("content-type", "text/turtle")
		.end(this.postQuery);
    }
}
