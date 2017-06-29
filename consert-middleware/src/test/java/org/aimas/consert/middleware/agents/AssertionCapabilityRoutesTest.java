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
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for AssertionCapability routes
 */
@RunWith(VertxUnitRunner.class)
public class AssertionCapabilityRoutesTest
{
	private final String CONFIG_FILE = "agents.properties";
			
	private Vertx vertx;
	private AgentConfig ctxCoord;
	
	
	@Before
	public void setUp(TestContext context) throws IOException {
		
		this.vertx = Vertx.vertx();

		Configuration config;
		try {
			config = new PropertiesConfiguration(CONFIG_FILE);
			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		vertx.deployVerticle(CtxCoord.class.getName(), context.asyncAssertSuccess());	
	}
	
    @After
    public void tearDown(TestContext context) {
    	//vertx.close(context.asyncAssertSuccess());
    }
    
    @Test
    public void testPostAndGetAll(TestContext context) {
    	
    	Async async = context.async();
    	
    	// POST
    	
    	String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
    			+ "@prefix core: <http://pervasive.semanticweb.org/ont/2014/05/consert/core#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#> .\n"
				+ "@prefix assertion-capability: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapability/> .\n"
				+ "@prefix context-assertion: <http://pervasive.semanticweb.org/ont/2014/05/consert/core#ContextAssertion/> .\n"
				+ "@prefix context-annotation: <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#ContextAnnotation/> .\n"
				+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/> .\n"
				+ "@prefix context-entity: <http://pervasive.semanticweb.org/ont/2014/05/consert/core#ContextEntity/> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
				+ "protocol:AssertionCapability rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionCapability\"^^xsd:string .\n"
				+ "core:ContextAssertion rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextAssertion\"^^xsd:string .\n"
				+ "core:ContextEntity rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextEntity\"^^xsd:string .\n"
				+ "annotation:ContextAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextAnnotation\"^^xsd:string .\n"
				+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
				+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n\n"
				+ "assertion-capability:foo a protocol:AssertionCapability ;\n"
				+ "    protocol:hasContent context-assertion:assert1 ;\n"
				+ "    protocol:hasAnnotation context-annotation:ann1 ;\n"
				+ "    protocol:hasAnnotation context-annotation:ann2 ;\n"
				+ "    protocol:hasProvider agent-spec:CtxSensor .\n"
				+ "context-assertion:assert1 a core:ContextAssertion ;\n"
				+ "    core:assertionRole context-entity:role1 .\n"
				+ "context-entity:role1 a core:ContextEntity ;\n"
				+ "    core:derivedDataAssertion <http://pervasive.semanticweb.org/ont/2014/05/consert/core#derivedDataAssertion/dda1> ;\n"
				+ "    core:derivedRelationAssertion context-entity:role1 ;\n"
				+ "    core:entityDataAssertion <http://pervasive.semanticweb.org/ont/2014/05/consert/core#entityDataAssertion/eda1> ;\n"
				+ "    core:entityDataDescription <http://pervasive.semanticweb.org/ont/2014/05/consert/core#entityDataDescription/edd1> ;\n"
				+ "    core:entityRelationAssertion context-entity:role1 ;\n"
				+ "    core:entityRelationDescription context-entity:role1 ;\n"
				+ "    core:profiledDataAssertion <http://pervasive.semanticweb.org/ont/2014/05/consert/core#profiledDataAssertion/pda1> ;\n"
				+ "    core:sensedDataAssertion <http://pervasive.semanticweb.org/ont/2014/05/consert/core#sensedDataAssertion/sda1> ;\n"
				+ "    core:sensedRelationAssertion context-entity:role1 .\n"
				+ "context-annotation:ann1 a annotation:ContextAnnotation .\n"
				+ "context-annotation:ann2 a annotation:ContextAnnotation .\n"
				+ "agent-spec:CtxSensor a protocol:AgentSpec ;\n"
				+ "    protocol:hasAddress agent-address:CtxSensorAddress ;\n"
				+ "    protocol:hasIdentifier \"CtxSensor1\" .\n"
				+ "agent-address:CtxSensorAddress a protocol:AgentAddress ;\n"
				+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
				+ "    protocol:port \"8080\"^^xsd:int .\n";
		
		this.vertx.createHttpClient()
			.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
					"/api/v1/coordination/context_assertions/", new Handler<HttpClientResponse>() {
				
				@Override
				public void handle(HttpClientResponse resp) {
					
					if(resp.statusCode() != 201) {
						context.fail("Failed to create AssertionCapability");
						async.complete();
					} else {
						
						// GET all
						
						vertx.createHttpClient()
							.get(ctxCoord.getPort(), ctxCoord.getAddress(), 
									"/api/v1/coordination/context_assertions/?agentIdentifier=CtxSensor1",
									new Handler<HttpClientResponse>() {
							
								@Override
								public void handle(HttpClientResponse resp) {
									
									if(resp.statusCode() != 200) {
										context.fail("Failed to get all AssertionCapabilities");
										async.complete();
									}
									
									resp.bodyHandler(new Handler<Buffer>() {
										
										@Override
										public void handle(Buffer buffer) {

											context.assertEquals("<http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapability/foo> <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#hasAnnotation> <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#ContextAnnotation/ann1> , <http://pervasive.semanticweb.org/ont/2017/06/consert/annotation#ContextAnnotation/ann2> ;"
													+ "<http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#hasContent> <http://pervasive.semanticweb.org/ont/2014/05/consert/core#ContextAssertion/assert1> ;"
													+ "<http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#hasProvider> <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/CtxSensor> ;"
													+ "a <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapability> .",
													buffer.toString().trim().replace("\r", "").replace("\n", "").replace("\t", ""));
											async.complete();
										}
									});
								}
							})
							.end();
					}
				}
			})
			.putHeader("content-type", "text/turtle")
			.end(rdfData);
    }
}
