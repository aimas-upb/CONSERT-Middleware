package org.aimas.consert.middleware.agents;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for AssertionCapability routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextQueryRouteTest {

	private final String CONFIG_FILE = "agents.properties";
	private final String sparqlQuery =
			  "PREFIX hlatest: <http://example.org/hlatest/>\n"
			+ "PREFIX annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#>\n"
			+ "SELECT ?assert \n"
			+ "WHERE {\n"
			+ "    ?assert hlatest:hasPersonRole <person:mihai> .\n"
			+ "    ?assert hlatest:hasAreaRole <area:WORK_AREA> .\n"
			+ "    ?assert <annotation:hasAnnotation> ?ann .\n"
			+ "    ?ann annotation:duration ?duration .\n"
			+ "    FILTER (?duration >= 0) .\n"
			+ "}\n";
	
	private final String startQueryPosition = "@prefix hlatest: <http://example.org/hlatest/> .\n"
			+ "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix start-updates-command: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StartUpdatesCommand/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			
			+ "protocol:StartUpdatesCommand rdfbeans:bindingClass \"org.aimas.consert.middleware.model.StartUpdatesCommand\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n"
			+ "hlatest:Position rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.Position\"^^xsd:string .\n"
			+ "\n"
			+ "start-updates-command:start-command a protocol:StartUpdatesCommand ;\n"
			+ "    protocol:hasTargetAssertion hlatest:Position ;\n"
			+ "    protocol:hasTargetAgent agent-spec:CtxSensorPosition .\n"
			
			+ "agent-spec:CtxSensorPosition a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxSensorAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxSensorPosition\" .\n"
			+ "agent-address:CtxSensorAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8085\"^^xsd:int .\n";
	
	private final int CTX_SENSOR_ID = 1;
            
	private Vertx vertx;
	private AgentConfig ctxQueryHandler;
	private HttpClient httpClient;

	@Before
	public void setUp(TestContext context) throws IOException, ConfigurationException {

		// Read configuration files
		Configuration config;

		config = new PropertiesConfiguration(CONFIG_FILE);
		this.ctxQueryHandler = AgentConfig.readCtxQueryHandlerConfig(config);

		// Start Vert.x server for CtxQueryHandler
		Async async = context.async();
		this.vertx = Vertx.vertx();
		
		config = new PropertiesConfiguration(CONFIG_FILE);
		AgentConfig ctxSensor = AgentConfig.readCtxSensorConfig(config).get(CTX_SENSOR_ID);
		JsonObject ctxSensorConfig = new JsonObject().put("id", CTX_SENSOR_ID);
		
		
		this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			this.vertx.deployVerticle(CtxSensorPosition.class.getName(), new DeploymentOptions().setWorker(true).setConfig(ctxSensorConfig), context.asyncAssertSuccess());
			this.vertx.deployVerticle(CtxQueryHandler.class.getName(), new DeploymentOptions().setWorker(true), context.asyncAssertSuccess());
			
			this.httpClient.put(ctxSensor.getPort(), ctxSensor.getAddress(),
					"/api/v1/sensing/tasking_command/", new Handler<HttpClientResponse>() {

						@Override
						public void handle(HttpClientResponse resp) {

							if (resp.statusCode() != 200) {
								context.fail("Failed to start updates, code " + resp.statusCode() + ": " + resp.statusMessage());
							}
							
							// wait for some events to be inserted
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							async.complete();
						}
					}).putHeader("content-type", "text/turtle").end(this.startQueryPosition);
		});

		this.httpClient = this.vertx.createHttpClient();
	}

	@After
	public void tearDown(TestContext context) {		
		this.vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testQuery(TestContext context) {
		
		Async async = context.async();

		this.httpClient.get(this.ctxQueryHandler.getPort(), this.ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_query/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to query context, code " + resp.statusCode());
							async.complete();
						} else {

							resp.bodyHandler(new Handler<Buffer>() {

								@Override
								public void handle(Buffer buffer) {
									
									System.out.println("\nresult of query: " + buffer.toString() + "\n");
									context.assertNotEquals(buffer.toString(), "");
									async.complete();
								}

							});
						}
					}
				}).putHeader("content-type", "text/turtle").end(this.sparqlQuery);
	}
}