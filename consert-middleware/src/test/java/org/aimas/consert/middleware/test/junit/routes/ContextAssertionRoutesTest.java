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
 * Unit test for ContextAssertion routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextAssertionRoutesTest {

	private Vertx vertx;
	private AgentConfig ctxCoord;
	private HttpClient httpClient;
	
	@Before
	public void setUp(TestContext context) throws IOException {

		this.ctxCoord = new AgentConfig("127.0.0.1", 8081);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		
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
	public void testCreateContextAssertionInstance(TestContext context) {
		
		Async async = context.async();
		
		String rdf = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix core: <http://pervasive.semanticweb.org/ont/2017/07/consert/core#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#> .\n"
				+ "@prefix hlatest: <http://example.org/hlatest/> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n"
				+ "\n"
				+ "hlatest:SittingLLA rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.SittingLLA\"^^xsd:string .\n"
				+ "hlatest:Person rdfbeans:bindingClass \"org.aimas.consert.tests.hla.entities.Person\"^^xsd:string .\n"
				+ "annotation:DefaultAnnotationData rdfbeans:bindingClass \"org.aimas.consert.model.annotations.DefaultAnnotationData\"^^xsd:string .\n"
				+ "hlatest:LLAType rdfbeans:bindingClass \"org.aimas.consert.tests.hla.entities.LLAType\"^^xsd:string .\n"
				+ "core:assertion-1 rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.SittingLLA\"^^xsd:string .\n"
				+ "\n"
				+ "protocol:assertionGraph {\n"
				+ "    core:assertion-1 a hlatest:SittingLLA ;\n"
				+ "        hlatest:hasLLATypeRole <lla:SITTING> ;\n"
				+ "        hlatest:hasPersonRole <person:mihai> ;\n"
				+ "        <annotation:hasAnnotation> annotation:annotation-1 .\n"
				
				+ "    <lla:SITTING> a hlatest:LLAType ;\n"
				+ "        rdfs:label \"SITTING\" .\n"
				
				+ "    <person:mihai> a hlatest:Person ;\n"
				+ "        hlatest:name \"mihai\" .\n"
				+ "}\n"
				
				+ "protocol:annotationGraph {\n"
				+ "    annotation:annotation-1 a annotation:DefaultAnnotationData ;\n"
				+ "        annotation:confidence \"0.8557057441203378\"^^xsd:double ;\n"
				+ "        annotation:duration \"0\"^^xsd:long ;\n"
				+ "        annotation:endTime \"2017-05-12T14:41:41.000+03:00\"^^xsd:dateTime ;\n"
				+ "        annotation:lastUpdated \"1.494589301E12\"^^xsd:double ;\n"
				+ "        annotation:startTime \"2017-05-12T14:41:41.000+03:00\"^^xsd:dateTime ;\n"
				+ "        annotation:timestamp \"1.494589301E12\"^^xsd:double .\n"
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
