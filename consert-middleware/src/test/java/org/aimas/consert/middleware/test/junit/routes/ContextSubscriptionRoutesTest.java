package org.aimas.consert.middleware.test.junit.routes;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.agents.OrgMgr;
import org.aimas.consert.middleware.api.MiddlewareAPI;
import org.aimas.consert.middleware.api.MiddlewareAPIImpl;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.protocol.ContextSubscriptionRequest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for ContextSubscription routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextSubscriptionRoutesTest {

	private final String postQuery = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix request: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscriptionRequest/> .\n"
			+ "@prefix context-subscription: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"

			+ "protocol:ContextSubscriptionRequest rdfbeans:bindingClass \"org.aimas.consert.middleware.protocol.ContextSubscriptionRequest\"^^xsd:string .\n"
			+ "protocol:ContextSubscription rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextSubscription\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n\n"
			
			+ "request:CtxSubsRequest a protocol:ContextSubscriptionRequest ;\n"
			+ "    protocol:hasInitiatorURI \"http://initiator-uri.org\"^^xsd:anyURI ;\n"
			+ "    protocol:hasInitiatorCallbackURI \"http://initiator-callback-uri.org\"^^xsd:anyURI ;\n"
			+ "    protocol:hasContextSubscription context-subscription:foo .\n"
			
			+ "context-subscription:foo a protocol:ContextSubscription ;\n"
			+ "    protocol:hasSubscriptionQuery \"the subscription query\"^^xsd:string ;\n"
			+ "    protocol:hasSubscriber agent-spec:CtxUser .\n"
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser1\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8083\"^^xsd:int .\n";

	private Vertx vertx;
	private AgentConfig ctxQueryHandler;
	private String resourceUUID;
	private HttpClient httpClient;
	private Repository repo;

	@Before
	public void setUp(TestContext context) throws IOException {
		
		Async async = context.async();

		this.ctxQueryHandler = new AgentConfig("127.0.0.1", 8082);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res1 -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), res2 -> {
				this.vertx.deployVerticle(CtxQueryHandler.class.getName(), new DeploymentOptions().setWorker(true), res3 -> {
					async.complete();
				});
			});
		});

		this.httpClient = this.vertx.createHttpClient();

		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		
		async.await();
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
				}).putHeader("content-type", "text/turtle").end(this.postQuery);
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

								context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxUser> ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriptionQuery> \"the subscription query\" ;"
												+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription> ."));
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

												context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
														"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxQueryHandler> ;"
																+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriptionQuery> \"the subscription query\" ;"
																+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription> ."));
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

	@Test
	public void testAPI(TestContext context) {

		ContextSubscriptionRequest request = new ContextSubscriptionRequest();
		request.setInitiatorURI(URI.create("http://initiator-uri.org"));
		request.setInitiatorCallbackURI(URI.create("http://initiator-callback-uri.org"));
		
		ContextSubscription subscription = new ContextSubscription();
		subscription.setSubscriptionQuery("the subscription query");
		subscription.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo");
		
		AgentSpec ctxUser = new AgentSpec();
		ctxUser.setIdentifier("CtxUser1");
		
		AgentAddress ctxUserAddr = new AgentAddress();
		ctxUserAddr.setIpAddress("127.0.0.1");
		ctxUserAddr.setPort(8083);
		
		ctxUser.setAddress(ctxUserAddr);
		subscription.setSubscriber(ctxUser);
		request.setContextSubscription(subscription);
		
		MiddlewareAPI api = new MiddlewareAPIImpl();
		this.resourceUUID = api.subscribeContextUpdates(request).toString();

		Async async = context.async();

		// GET one

		this.httpClient.get(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to get ContextSubscription");
							async.complete();
						}

						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								
								context.assertTrue(buffer.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription/foo>"));
								async.complete();
							}
						});
					}
				}).end();
	}
}
