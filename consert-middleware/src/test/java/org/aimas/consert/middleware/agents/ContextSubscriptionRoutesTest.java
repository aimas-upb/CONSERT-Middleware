package org.aimas.consert.middleware.agents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.aimas.consert.middleware.protocol.ContextSubscriptionResource;
import org.aimas.consert.middleware.protocol.RequestBean;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
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
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for ContextSubscription routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextSubscriptionRoutesTest {
	private final String CONFIG_FILE = "agents.properties";
	private final String postQuery = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix context-subscription: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			+ "protocol:ContextSubscription rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextSubscription\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n\n"
			+ "context-subscription:foo a protocol:ContextSubscription ;\n"
			+ "    protocol:hasSubscriptionQuery \"the subscription query\"^^xsd:string ;\n"
			+ "    protocol:hasSubscriber agent-spec:CtxUser .\n"
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser1\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8081\"^^xsd:int .\n";

	private Vertx vertx;
	private AgentConfig ctxQueryHandler;
	private String resourceUUID;
	private HttpClient httpClient;
	private Repository repo;

	@Before
	public void setUp(TestContext context) throws IOException, ConfigurationException {

		// Read configuration files
		Configuration config;

		config = new PropertiesConfiguration(CONFIG_FILE);
		this.ctxQueryHandler = AgentConfig.readCtxQueryHandlerConfig(config);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		this.vertx.deployVerticle(CtxQueryHandler.class.getName(), context.asyncAssertSuccess());

		this.httpClient = this.vertx.createHttpClient();

		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
	}

	@After
	public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
		this.repo.shutDown();
	}

	@Test
	public void testPost(TestContext context) {

		Async async = context.async();
		this.post(context, async);
		async.await();
	}

	public void post(TestContext context, Async async) {

		// POST: insert data that we will try to fetch in the test methods

		RequestBean reqBean = new RequestBean("initiatorURI", "initiatorCallbackURI", this.postQuery);

		this.httpClient.post(this.ctxQueryHandler.getPort(), this.ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_subscriptions/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 201) {
							context.fail("Failed to create ContextSubscription, code " + resp.statusCode());
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
				}).putHeader("content-type", "text/turtle").end(Json.encodePrettily(reqBean));
	}

	@Test
	public void testGetOne(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		// GET one

		this.httpClient.get(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp2) {

						if (resp2.statusCode() != 200) {
							context.fail("Failed to get ContextSubscription");
							async.complete();
						}

						resp2.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer2) {

								ContextSubscriptionResource res = Json.decodeValue(buffer2.toString(),
										ContextSubscriptionResource.class);

								String rdf = null;
								try {
									rdf = contextSubscriptionResourceToRDF(res);
								} catch (RepositoryException | RDFBeanException e) {
									e.printStackTrace();
									context.fail("Failed to convert ContextSubscriptionResource to RDF");
									async.complete();
								}

								context.assertEquals(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxUser> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriptionQuery> \"the subscription query\" ;"
												+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription> .",
										rdf.trim().replace("\r", "").replace("\n", "").replace("\t", ""));
								async.complete();
							}
						});
					}
				}).end();
	}

	@Test
	public void testPut(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		String updated = this.postQuery.replace("CtxUser", "CtxQueryHandler");

		RequestBean reqBean = new RequestBean("initiatorURI", "initiatorCallbackURI", updated);

		// PUT

		this.httpClient.put(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to get ContextSubscription");
							async.complete();
						}

						// GET one

						httpClient.get(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
								"/api/v1/dissemination/context_subscriptions/" + resourceUUID + "/",
								new Handler<HttpClientResponse>() {

									@Override
									public void handle(HttpClientResponse resp2) {

										if (resp2.statusCode() != 200) {
											context.fail("Failed to get ContextSubscription");
											async.complete();
										}

										resp2.bodyHandler(new Handler<Buffer>() {

											@Override
											public void handle(Buffer buffer2) {

												ContextSubscriptionResource res = Json.decodeValue(buffer2.toString(),
														ContextSubscriptionResource.class);

												String rdf = null;
												try {
													rdf = contextSubscriptionResourceToRDF(res);
												} catch (RepositoryException | RDFBeanException e) {
													e.printStackTrace();
													context.fail(
															"Failed to convert ContextSubscriptionResource to RDF");
													async.complete();
												}

												context.assertEquals(
														"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxQueryHandler> ;"
																+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriptionQuery> \"the subscription query\" ;"
																+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription> .",
														rdf.trim().replace("\r", "").replace("\n", "").replace("\t",
																""));
												async.complete();
											}
										});
									}
								}).end();
					}
				}).putHeader("content-type", "text/turtle").end(Json.encodePrettily(reqBean));
	}

	@Test
	public void testDelete(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		// DELETE

		this.httpClient.delete(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to delete ContextSubscription");
							async.complete();
						}

						// GET one

						httpClient.get(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
								"/api/v1/dissemination/context_subscriptions/" + resourceUUID + "/",
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

	private String contextSubscriptionResourceToRDF(ContextSubscriptionResource res)
			throws RepositoryException, RDFBeanException {

		RepositoryConnection conn = repo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		Resource r = manager.add(res.getContextSubscription());

		// Prepare to write RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();

		RepositoryResult<Statement> iter = conn.getStatements(r, null, null);

		// Write all the statements
		while (iter.hasNext()) {

			writer.handleStatement(iter.next());
		}

		writer.endRDF();

		conn.close();

		return baos.toString();
	}
}
