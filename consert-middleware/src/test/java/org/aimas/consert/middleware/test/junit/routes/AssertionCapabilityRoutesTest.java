package org.aimas.consert.middleware.test.junit.routes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.api.MiddlewareAPI;
import org.aimas.consert.middleware.api.MiddlewareAPIImpl;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.model.annotations.ContextAnnotation;
import org.aimas.consert.model.annotations.DatetimeInterval;
import org.aimas.consert.model.annotations.TemporalValidityAnnotation;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
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
public class AssertionCapabilityRoutesTest {

	private final String CONFIG_FILE = "agents.properties";
	private final String postQuery = "@prefix : <http://example.org/hlatest/> .\n"
			+ "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix core: <http://pervasive.semanticweb.org/ont/2017/07/consert/core#> .\n"
			+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#> .\n"
			+ "@prefix assertion-capability: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/> .\n"
			+ "@prefix owl: <http://www.w3.org/2002/07/owl#> ."
			+ "@prefix context-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix context-entity: <http://pervasive.semanticweb.org/ont/2017/07/consert/core#ContextEntity/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			
			+ ":LLA rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.LLA\"^^xsd:string .\n"
			+ "protocol:AssertionCapability rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionCapability\"^^xsd:string .\n"
			
			+ "annotation:ContextAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.ContextAnnotation\"^^xsd:string .\n"
			+ "annotation:DatetimeInterval rdfbeans:bindingClass \"org.aimas.consert.model.annotations.DatetimeInterval\"^^xsd:string .\n"
			+ "annotation:NumericTimestampAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.NumericTimestampAnnotation\"^^xsd:string .\n"
			+ "annotation:NumericCertaintyAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.NumericCertaintyAnnotation\"^^xsd:string .\n"
			+ "annotation:TemporalValidityAnnotation rdfbeans:bindingClass \"org.aimas.consert.model.annotations.TemporalValidityAnnotation\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n"
			+ "\n"
			+ "assertion-capability:foo a protocol:AssertionCapability ;\n"
			+ "    protocol:hasContent :LLA ;\n"
			+ "    annotation:hasAnnotation context-annotation:ann1 ;\n"
			+ "    annotation:hasAnnotation context-annotation:ann2 ;\n"
			+ "    annotation:hasAnnotation context-annotation:ann3 ;\n"
			+ "    protocol:hasProvider agent-spec:CtxSensor .\n"
			+ "context-annotation:ann1 a annotation:NumericTimestampAnnotation ;\n"
			+ "    annotation:hasContinuityFunction \"continuity function\"^^xsd:string ;\n"
			+ "    annotation:hasJoinOperator \"join operator\"^^xsd:string ;\n"
			+ "    annotation:hasMeetOperator \"meet operator\"^^xsd:string ;\n"
			+ "    annotation:hasValue \"123456789\"^^xsd:double .\n"
			+ "context-annotation:ann2 a annotation:NumericCertaintyAnnotation ;\n"
			+ "    annotation:hasContinuityFunction \"continuity function\"^^xsd:string ;\n"
			+ "    annotation:hasJoinOperator \"join operator\"^^xsd:string ;\n"
			+ "    annotation:hasMeetOperator \"meet operator\"^^xsd:string ;\n"
			+ "    annotation:hasValue \"0.8\"^^xsd:decimal .\n"
			+ "context-annotation:ann3 a annotation:TemporalValidityAnnotation ;\n"
			+ "    annotation:hasContinuityFunction \"continuity function\"^^xsd:string ;\n"
			+ "    annotation:hasJoinOperator \"join operator\"^^xsd:string ;\n"
			+ "    annotation:hasMeetOperator \"meet operator\"^^xsd:string ;\n"
			+ "    annotation:hasValue [ \n"
			+ "        a annotation:DatetimeInterval ;\n"
			+ "        annotation:startTime \"2017-07-11T12:34:56Z\"^^xsd:dateTime ;\n"
			+ "        annotation:endTime \"2017-07-11T12:43:56Z\"^^xsd:dateTime \n"
			+ "    ] .\n"
			+ "agent-spec:CtxSensor a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxSensorAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxSensor1\" .\n"
			+ "agent-address:CtxSensorAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8080\"^^xsd:int .\n";
            

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
		this.post(context, async, this.postQuery);
		async.await();
	}

	public void post(TestContext context, Async async, String data) {

		// POST: insert data that we will try to fetch in the test methods

		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/context_assertions/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 201) {
							context.fail("Failed to create AssertionCapability, code " + resp.statusCode());
							async.complete();
						} else {

							// Get the created resource's UUID to make requests
							// on it later
							resp.bodyHandler(new Handler<Buffer>() {

								@Override
								public void handle(Buffer buffer) {
									resourceUUID = buffer.toString();
									async.complete();
								}

							});
						}
					}
				}).putHeader("content-type", "text/turtle").end(data);
	}

	@Test
	public void testGetAll(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery);
		asyncPost.await();

		Async async = context.async();

		// GET all

		this.httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/context_assertions/?agentIdentifier=CtxSensor1",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to get all AssertionCapabilities");
							async.complete();
						}

						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {

								context.assertTrue(buffer.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#hasAnnotation> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann1> , <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann2> , <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann3> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasContent> <http://example.org/hlatest/LLA> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasProvider> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxSensor> ;"
												+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability> ."));
								async.complete();
							}
						});
					}
				}).end();
	}

	@Test
	public void testGetOne(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery);
		asyncPost.await();

		Async async = context.async();

		// GET one

		this.httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/context_assertions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp2) {

						if (resp2.statusCode() != 200) {
							context.fail("Failed to get AssertionCapability");
							async.complete();
						}

						resp2.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer2) {
								
								Repository repo = new SailRepository(new MemoryStore());
								repo.initialize();
								RepositoryConnection conn = repo.getConnection();
								RDFBeanManager manager = new RDFBeanManager(conn);

								// Check datetime
								try {

									Model model = Rio.parse(new ByteArrayInputStream(buffer2.getBytes()), "", RDFFormat.TURTLE);
									conn.add(model);

									AssertionCapability ac = (AssertionCapability) manager.get("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/foo", AssertionCapability.class);
									
									for(ContextAnnotation ann : ac.getAnnotations()) {
										if(ann instanceof TemporalValidityAnnotation) {
											
											DatetimeInterval interval = (DatetimeInterval) ann.getValue();
											
											context.assertEquals(1499776496000L, interval.getStart().getTime());
											context.assertEquals(1499777036000L, interval.getEnd().getTime());
										}
									}									

								} catch (RDFParseException | UnsupportedRDFormatException | RepositoryException | IOException | RDFBeanException e) {
									e.printStackTrace();
								}

								// Check statements
								context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t","").contains(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#hasAnnotation> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann1> , <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann2> , <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann3> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasContent> <http://example.org/hlatest/LLA> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasProvider> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxSensor> ;"
												+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability> ."));
								async.complete();
							}
						});
					}
				}).end();
	}

	@Test
	public void testPut(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery);
		asyncPost.await();

		Async async = context.async();

		String updated = this.postQuery.replace("    annotation:hasAnnotation context-annotation:ann2 ;\n", "");

		// PUT

		this.httpClient.put(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/context_assertions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to get AssertionCapability");
							async.complete();
						}

						// GET one

						httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
								"/api/v1/coordination/context_assertions/" + resourceUUID + "/",
								new Handler<HttpClientResponse>() {

									@Override
									public void handle(HttpClientResponse resp2) {

										if (resp2.statusCode() != 200) {
											context.fail("Failed to get AssertionCapability");
											async.complete();
										}

										resp2.bodyHandler(new Handler<Buffer>() {

											@Override
											public void handle(Buffer buffer2) {

												context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
														"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#hasAnnotation> <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann1> , <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/ann3> ;"
																+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasContent> <http://example.org/hlatest/LLA> ;"
																+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasProvider> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxSensor> ;"
																+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability> ."));
												async.complete();
											}
										});
									}
								}).end();
					}
				}).putHeader("content-type", "text/turtle").end(updated);
	}

	@Test
	public void testDelete(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery);
		asyncPost.await();
		
		asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery.replace("foo", "foo2"));
		asyncPost.await();

		Async async = context.async();

		// DELETE

		this.httpClient.delete(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/context_assertions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to delete AssertionCapability");
							async.complete();
						}

						// GET one

						httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
								"/api/v1/coordination/context_assertions/" + resourceUUID + "/",
								new Handler<HttpClientResponse>() {

									@Override
									public void handle(HttpClientResponse resp2) {

										context.assertEquals(404, resp2.statusCode());
										async.complete();
									}
								}).end();
					}
				}).end();
	}

	@Test
	public void testAPI(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost, this.postQuery);
		asyncPost.await();
		
		List<AgentSpec> expected = new ArrayList<AgentSpec>();
		AgentSpec ctxSensor = new AgentSpec();
		ctxSensor.setIdentifier("CtxSensor1");
		ctxSensor.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxSensor");
		
		AgentAddress address = new AgentAddress();
		address.setPort(8080);
		address.setIpAddress("127.0.0.1");
		
		ctxSensor.setAddress(address);
		expected.add(ctxSensor);

		MiddlewareAPI api = new MiddlewareAPIImpl();
		List<AgentSpec> res = api.listProviders(URI.create("http://example.org/hlatest/LLA"));
		
		context.assertEquals(expected, res);
	}
}