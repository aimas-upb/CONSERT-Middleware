package org.aimas.consert.middleware.test.junit.routes;

import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.OrgMgr;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
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
	

	private static final String POST_QUERY = "@prefix : <http://example.org/hlatest/> .\n"
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
	public void setUp(TestContext context) throws IOException {

		this.ctxCoord = new AgentConfig("127.0.0.1", 8081);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		
		// Deploy the required verticles for the queries
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), context.asyncAssertSuccess());
		});

		this.httpClient = this.vertx.createHttpClient();
	}

	@After
	public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
	}
	
	@Test
	public void testStaticContextInsertion(TestContext context) {
		
		Async async = context.async();
		
		// Send a static context assertion to the CtxCoord agent for its insertion
		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/insert_entity_descriptions/", new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {

				if (resp.statusCode() != 200) {
					context.fail("Failed to get static context insertion, code " + resp.statusCode());
				}
				
				async.complete();
			}
		}).putHeader("content-type", "text/turtle").end(POST_QUERY);
	}
	
	@Test
	public void testStaticContextUpdate(TestContext context) {
		
		Async async = context.async();
		
		// Send a static context update to the CtxCoord agent
		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/update_entity_descriptions/", new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {

				if (resp.statusCode() != 200) {
					context.fail("Failed to get static context update, code " + resp.statusCode());
				}
				
				async.complete();
			}
		}).putHeader("content-type", "text/turtle").end(POST_QUERY);
	}
}
