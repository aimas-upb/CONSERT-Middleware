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
 * Unit test for ContextEntity routes
 */
@RunWith(VertxUnitRunner.class)
public class StaticContextRoutesTest {

	private Vertx vertx;
	private AgentConfig ctxCoord;
	private HttpClient httpClient;
	private final String CONFIG_FILE = "agents.properties";
	

	private final String postQuery = "@prefix : <http://example.org/hlatest/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n"
			+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n"
			
			+ ":Person rdfbeans:bindingClass \"org.aimas.consert.tests.hla.entities.Person\"^^xsd:string .\n"
			+ ":Area rdfbeans:bindingClass \"org.aimas.consert.tests.hla.entities.Area\"^^xsd:string .\n"
			+ "\n"
			+ ":Mihai a :Person ;\n"
			+ "    :name \"Mihai\"^^xsd:string .\n"
			+ ":WorkArea a :Area;\n"
			+ "    rdfs:label \"A work area\"^^xsd:string .\n";

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
	public void testStaticContextInsertion(TestContext context) {
		
		Async async = context.async();
		
		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/insert_entity_descriptions/", new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {

				if (resp.statusCode() != 200) {
					context.fail("Failed to get static context insertion, code " + resp.statusCode());
				}
				
				async.complete();
			}
		}).putHeader("content-type", "text/turtle").end(this.postQuery);
	}
	
	@Test
	public void testStaticContextUpdate(TestContext context) {
		
		Async async = context.async();
		
		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/update_entity_descriptions/", new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {

				if (resp.statusCode() != 200) {
					context.fail("Failed to get static context update, code " + resp.statusCode());
				}
				
				async.complete();
			}
		}).putHeader("content-type", "text/turtle").end(this.postQuery);
	}
}
