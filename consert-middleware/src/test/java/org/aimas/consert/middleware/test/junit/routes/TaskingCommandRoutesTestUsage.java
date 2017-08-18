package org.aimas.consert.middleware.test.junit.routes;

import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxUser;
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
 * Unit test for TaskingCommand routes
 */
@RunWith(VertxUnitRunner.class)
public class TaskingCommandRoutesTestUsage {

	private final String startQuery = "@prefix hlatest: <http://example.org/hlatest/> .\n"
			+ "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix start-updates-command: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StartUpdatesCommand/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			
			+ "protocol:StartUpdatesCommand rdfbeans:bindingClass \"org.aimas.consert.middleware.model.tasking.StartUpdatesCommand\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n"
			+ "hlatest:Position rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.Position\"^^xsd:string .\n"
			+ "\n"
			+ "start-updates-command:start-command a protocol:StartUpdatesCommand ;\n"
			+ "    protocol:hasTargetAssertion hlatest:Position ;\n"
			+ "    protocol:hasTargetAgent agent-spec:CtxUser .\n"
			
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8082\"^^xsd:int .\n";

	private final String stopQuery = startQuery.replace("start-", "stop").replace("StartUpdatesCommand", "StopUpdatesCommand");

	private final String alterQuery = "@prefix hlatest: <http://example.org/hlatest/> .\n"
			+ "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix provisioning: <http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/coordconf#> .\n"
			+ "@prefix alter-update-mode-command: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AlterUpdateModeCommand/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix update-mode: <http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/coordconf#AssertionUpdateMode/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			
			+ "protocol:StartUpdatesCommand rdfbeans:bindingClass \"org.aimas.consert.middleware.model.tasking.StartUpdatesCommand\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n"
			+ "provisioning:AssertionUpdateMode rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionUpdateMode\"^^xsd:string .\n"
			+ "hlatest:Position rdfbeans:bindingClass \"org.aimas.consert.tests.hla.assertions.Position\"^^xsd:string .\n"
			+ "\n"
			+ "alter-update-mode-command:alter-update-mode-command a protocol:AlterUpdateModeCommand ;\n"
			+ "    protocol:hasTargetAssertion hlatest:Position ;\n"
			+ "    protocol:hasTargetAgent agent-spec:CtxUser ;\n"
			+ "    protocol:hasUpdateMode update-mode:NewUpdateMode .\n"
			
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8082\"^^xsd:int .\n"
			
			+ "update-mode:NewUpdateMode a provisioning:AssertionUpdateMode ;\n"
			+ "    provisioning:hasMode \"change-based\"^^xsd:string ;\n"
			+ "    provisioning:hasUpdateRate \"0\"^^xsd:int .\n";
            
	private Vertx vertx;
	private AgentConfig ctxUser;
	private HttpClient httpClient;

	@Before
	public void setUp(TestContext context) throws IOException {

		this.ctxUser = new AgentConfig("127.0.0.1", 8082);

		// Start Vert.x server for CtxUser
		Async async = context.async();
		
		this.vertx = Vertx.vertx();
		
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res1 -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), res2 -> {
				this.vertx.deployVerticle(CtxUser.class.getName(), new DeploymentOptions().setWorker(true), res3 -> {
					async.complete();
				});
			});
		});

		this.httpClient = this.vertx.createHttpClient();
	}

	@After
	public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
	}

	public void start(TestContext context, Async async) {

		// Send tasking command
		this.httpClient.put(this.ctxUser.getPort(), this.ctxUser.getAddress(),
				"/api/v1/usage/tasking_command/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to start updates, code " + resp.statusCode() + ": " + resp.statusMessage());
						}
						
						async.complete();
					}
				}).putHeader("content-type", "text/turtle").end(this.startQuery);
	}
	
	@Test
	public void testStart(TestContext context) {
		
		start(context, context.async());
	}

	@Test
	public void testStop(TestContext context) {
		
		Async startAsync = context.async();
		start(context, startAsync);
		startAsync.await();
		
		Async async = context.async();

		// Send tasking command
		this.httpClient.put(this.ctxUser.getPort(), this.ctxUser.getAddress(),
				"/api/v1/usage/tasking_command/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to stop updates, code " + resp.statusCode() + ": " + resp.statusMessage());
						}
						
						async.complete();
					}
				}).putHeader("content-type", "text/turtle").end(this.stopQuery);
	}

	@Test
	public void testAlter(TestContext context) {
		
		Async startAsync = context.async();
		start(context, startAsync);
		startAsync.await();
		
		Async async = context.async();

		// Send tasking command
		this.httpClient.put(this.ctxUser.getPort(), this.ctxUser.getAddress(),
				"/api/v1/usage/tasking_command/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to alter update mode, code " + resp.statusCode() + ": " + resp.statusMessage());
						}
						
						async.complete();
					}
				}).putHeader("content-type", "text/turtle").end(this.alterQuery);
	}
}