package org.aimas.consert.middleware.test.junit;

import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.agents.OrgMgr;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for the use of websockets
 */
@RunWith(VertxUnitRunner.class)
public class WebsocketTest {
	
	private static final String QUERY_CONTEXT_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.DISSEMINATION_ROUTE + "/context_query/";
	
	private static final String SPARQL_QUERY =
			  "PREFIX hlatest: <http://example.org/hlatest/>\n"
			+ "PREFIX annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#>\n"
			+ "SELECT ?assert \n"
			+ "WHERE {\n"
			+ "    ?assert a hlatest:Position .\n"
			+ "	   ?assert hlatest:hasPersonRole <person:mihai> .\n"
			+ "    ?assert hlatest:hasAreaRole <area:WORK_AREA> .\n"
			+ "    ?assert <annotation:hasAnnotation> ?ann .\n"
			+ "    ?ann annotation:timestamp ?timestamp .\n"
			+ "    FILTER (?timestamp >= 1494589332000) .\n"
			+ "}\n";

	private Vertx vertx;
	private AgentConfig ctxQueryHandler;
	private HttpClient httpClient;
	
	@Before
	public void setUp(TestContext context) throws IOException {

		// Start Vert.x server for CtxQueryHandler
		this.vertx = Vertx.vertx();
		
		this.ctxQueryHandler = new AgentConfig("127.0.0.1", 8082);
		
		Async async = context.async();
		
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res1 -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), res2 -> {
				this.vertx.deployVerticle(CtxQueryHandler.class.getName(), new DeploymentOptions().setWorker(true), res3 -> {
					async.complete();
				});
			});
		});

		this.httpClient = this.vertx.createHttpClient();
		
		async.await();
	}

	@After
	public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
	}
	
	@Test
	public void testQuery(TestContext context) {
		
		this.httpClient.websocket(this.ctxQueryHandler.getPort(), this.ctxQueryHandler.getAddress(),
				WebsocketTest.QUERY_CONTEXT_ROUTE, new Handler<WebSocket>() {

			@Override
			public void handle(WebSocket socket) {
				
				socket.writeTextMessage(WebsocketTest.SPARQL_QUERY);
			}
		});
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
}
