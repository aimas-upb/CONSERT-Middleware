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
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for ContextAssertion routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextAssertionRoutesTest {

	private final String CONFIG_FILE = "agents.properties";
	private Vertx vertx;
	private AgentConfig ctxCoord;
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
	public void testCreateContextAssertionInstance(TestContext context) {
		
		Async async = context.async();
		
		String rdf = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#> .\n"
				+ "@prefix assertion-instance: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionInstance/> .\n"
				+ "@prefix timestamp-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#TimestampAnnotation/> .\n"
				+ "@prefix certainty-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#CertaintyAnnotation/> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n"
				+ "\n"
				+ "protocol:AssertionInstance rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionInstance\"^^xsd:string .\n"
				+ "annotation:NumericTimestampAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.NumericTimestampAnnotation\"^^xsd:string .\n"
				+ "annotation:NumericCertaintyAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.NumericCertaintyAnnotation\"^^xsd:string .\n"
				+ "\n"
				+ "protocol:assertionGraph {\n"
				+ "    assertion-instance:assertionUpdate a protocol:AssertionInstance ;\n"
				+ "        protocol:hasContent assertion-instance:assertionContent .\n"
				+ "}\n"
				+ "protocol:annotationGraph {\n"
				+ "    assertion-instance:assertionUpdate annotation:hasAnnotation timestamp-annotation:ts .\n"
				+ "    timestamp-annotation:ts a annotation:NumericTimestampAnnotation .\n"
				+ "    certainty-annotation:certainty a annotation:NumericCertaintyAnnotation .\n"
				+ "}\n";
		
		
		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/insert_context_assertion/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 201) {
							context.fail("Failed to create ContextAssertion, code " + resp.statusCode());
						} /*else {
							
							resp.bodyHandler(new Handler<Buffer>() {

								@Override
								public void handle(Buffer buffer) {
								}

							});
						}*/
						
						async.complete();
					}
				}).putHeader("content-type", "application/trig").end(rdf);
	}
}
