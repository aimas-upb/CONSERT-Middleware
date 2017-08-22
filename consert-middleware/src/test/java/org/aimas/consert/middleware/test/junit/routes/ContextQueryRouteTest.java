package org.aimas.consert.middleware.test.junit.routes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.agents.OrgMgr;
import org.aimas.consert.middleware.protocol.RequestResource;
import org.aimas.consert.middleware.test.CtxSensorPosition;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for context query routes
 */
@RunWith(VertxUnitRunner.class)
public class ContextQueryRouteTest {
	
	private final static IRI RDF_TYPE = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private final static IRI REQUEST_RESOURCE_IRI = SimpleValueFactory.getInstance().createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#RequestResource");

	private final String sparqlQuery =
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
	
	private final String startQueryPosition = "@prefix hlatest: <http://example.org/hlatest/> .\n"
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
			+ "    protocol:hasTargetAgent agent-spec:CtxSensorPosition .\n"
			
			+ "agent-spec:CtxSensorPosition a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxSensorAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxSensorPosition\" .\n"
			+ "agent-address:CtxSensorAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8082\"^^xsd:int .\n";
            
	private Vertx vertx;
	private AgentConfig ctxQueryHandler;
	private HttpClient httpClient;
	
	private Repository convRepo;
	private RepositoryConnection convRepoConn;
	private RDFBeanManager convManager;

	@Before
	public void setUp(TestContext context) throws IOException {
		
		// Initialization of the conversion repository
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);
		
		// Start Vert.x server for CtxQueryHandler
		Async async = context.async();
		this.vertx = Vertx.vertx();
		
		AgentConfig ctxSensor = new AgentConfig("127.0.0.1", 8082);
		this.ctxQueryHandler = new AgentConfig("127.0.0.1", 8083);
		
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), res2 -> {
				this.vertx.deployVerticle(CtxSensorPosition.class.getName(), new DeploymentOptions().setWorker(true), res3 -> {
					this.vertx.deployVerticle(CtxQueryHandler.class.getName(), new DeploymentOptions().setWorker(true), res4 -> {
						
						this.httpClient.put(ctxSensor.getPort(), ctxSensor.getAddress(), "/api/v1/sensing/tasking_command/", new Handler<HttpClientResponse>() {
			
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
				});
			});
		});

		this.httpClient = this.vertx.createHttpClient();
	}

	@After
	public void tearDown(TestContext context) {
		this.convRepoConn.close();
		this.convRepo.shutDown();
		
		this.vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testQuery(TestContext context) {
		
		Async async = context.async();
		Future<Void> future = Future.future();

		this.httpClient.websocket(this.ctxQueryHandler.getPort(), this.ctxQueryHandler.getAddress(),
				"/api/v1/dissemination/context_query/", new Handler<WebSocket>() {

					@Override
					public void handle(WebSocket socket) {
						
						socket.writeTextMessage(sparqlQuery);

						socket.textMessageHandler(new Handler<String>() {

								@Override
								public void handle(String str) {
									
									StringBuilder result = new StringBuilder();
									
									try {
										
										// the UUID can be parsed, it was a long-lasting query
										UUID.fromString(str);
										System.out.println("long-lasting query");
										
										if(!future.isComplete()) {
											future.complete();
											return;
										}
										
										httpClient.get(ctxQueryHandler.getPort(), ctxQueryHandler.getAddress(), "/api/v1/dissemination/resources/" + str + "/", new Handler<HttpClientResponse>() {

											@Override
											public void handle(HttpClientResponse resp) {
												
												resp.bodyHandler(new Handler<Buffer>() {

													@Override
													public void handle(Buffer buffer) {
														
														// Convert the received statements to a Java object
														try {
															
															Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);

															convRepoConn.add(model);

															RepositoryResult<Statement> statements = convRepoConn.getStatements(null, ContextQueryRouteTest.RDF_TYPE, ContextQueryRouteTest.REQUEST_RESOURCE_IRI);

															while(statements.hasNext()) {
																
																Statement s = statements.next();

																RequestResource resource = convManager.get(s.getSubject(), RequestResource.class);
																result.append(resource.getStringResult());
															}
															
															System.out.println("\nresult of query: " + result.toString() + "\n");
															context.assertNotEquals(result.toString(), "");
															async.complete();
															
														} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
															e.printStackTrace();
														}
													}
												});
											}
										}).end();
										
									} catch(IllegalArgumentException e) {
										
										// the UUID cannot be parsed, it was a short-lasting query
										result.append(str);
										System.out.println("short-lasting query");
										
										System.out.println("\nresult of query: " + result.toString() + "\n");
										context.assertNotEquals(result.toString(), "");
										async.complete();
									}
								}
						});
					}
		});
	}
}