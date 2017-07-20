package org.aimas.consert.middleware.agents;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.Constants;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.LLA;
import org.aimas.consert.tests.hla.assertions.Position;
import org.aimas.consert.utils.JSONEventReader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * CtxSensor agent implemented as a Vert.x server
 */
public class CtxSensor extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties"; // path to the
															// configuration
															// file for this
															// agent
	
	private final String EVENTS_FILE_NAME = "files/single_hla_120s_01er_015fd.json";

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted
	private int id; // identifier to distinguish CtxSensor instances

	private Repository repo; // repository containing the RDF data

	private AgentConfig ctxCoord; // configuration to communicate with the
									// CtxCoord agent
	private AgentConfig orgMgr; // configuration to communicate with the OrgMgr
								// agent
	
	private ScheduledExecutorService readerService;  // reads the file containing the context assertions
	                                                 // and their annotations
	
	private boolean isFinished = false;     // allows to know when the CtxSensor has finished sending all the events
	private Queue<Object> events;           // list of the read events
	private Object syncObj = new Object();  // object used for the synchronization of the threads

	public static void main(String[] args) {

		// CtxSensor.vertx.deployVerticle(CtxSensor.class.getName());
	}

	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repository
		this.repo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		this.repo.initialize();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterSensing(vertx, this);

		// Read configuration
		this.id = this.config().getInteger("id");
		try {

			Configuration config = new PropertiesConfiguration(CONFIG_FILE);

			this.agentConfig = AgentConfig.readCtxSensorConfig(config).get(this.id);
			this.host = config.getString("CtxSensor.host");

			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);

		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}

		// Start server
		HttpServer server = this.vertx.createHttpServer();
		server.requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println(
								"Started CtxSensor on port " + server.actualPort() + " host " + this.host);
					} else {
						System.out.println("Failed to start CtxSensor on port " + server.actualPort() + " host "
								+ this.host);
					}

					future.complete();
				});
		
		
		// Start reading the context assertions and their annotations
		ClassLoader classLoader = CtxSensor.class.getClassLoader();
        File eventsFile = new File(classLoader.getResource(this.EVENTS_FILE_NAME).getFile());
		
		this.events = JSONEventReader.parseEvents(eventsFile);		
		this.readerService = Executors.newScheduledThreadPool(1);
		this.readerService.execute(new EventReadTask());
	}

	@Override
	public void stop() {
		this.repo.shutDown();
		this.readerService.shutdownNow();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}
	
	private class EventReadTask implements Runnable {
		
		private HttpClient client;
		private Repository repo;
		
		public EventReadTask() {
			this.client = vertx.createHttpClient();
			this.repo = new SailRepository(new MemoryStore());
			this.repo.initialize();
		}
		
		public void run() {
			// get event to be inserted
			ContextAssertion event = (ContextAssertion)events.poll();
			if (event != null) {
				
				// one CtxSensor sends LLA events, and the other sends position events
				if((id == 0 && event instanceof LLA)
						|| (id == 1 && event instanceof Position)) {
					
					System.out.println("CtxSensor " + id + " sends event " + event);
					this.sendEvent(event);					
				}
				
				// look at the next event if there is one
				ContextAssertion nextEvent = (ContextAssertion)events.peek();
				
				if (nextEvent != null) {
					//long delay = (long)(nextEvent.getStartTimestamp() - event.getStartTimestamp());
					int delay = 50;
					System.out.println("Next Event due in " + delay + " ms");
					
					readerService.schedule(new EventReadTask(), delay, TimeUnit.MILLISECONDS);
					//readerService.schedule(new EventReadTask(), 1, TimeUnit.MILLISECONDS);
				}
				else {
					setFinished(true);
					this.repo.shutDown();
				}
			}
			else {
				setFinished(true);
				this.repo.shutDown();
			}
			
			// Send a message to the CtxCoord when there is no more data
			if(isFinished()) {
				
				String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
						"/insert_context_assertion/";
				
				this.client.post(ctxCoord.getPort(), ctxCoord.getAddress(), route, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {}
					
				}).putHeader("content-type", "text/plain").end("finished");
			}
        }
		
		// Sends the event to the known CtxCoord
		private void sendEvent(/*AssertionInstance*/ ContextAssertion event) {
			
			// First, we need to convert the objects to RDF statements
			// We use the repository for this
			RepositoryConnection conn = this.repo.getConnection();
			RDFBeanManager manager = new RDFBeanManager(conn);
			
			try {
				manager.add(event);
			} catch (RepositoryException | RDFBeanException e) {
				e.printStackTrace();
			}
			
			// Prepare to write the RDF statements
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TRIG, baos);
			writer.startRDF();

			List<Statement> bindingStatements = new LinkedList<Statement>();
			List<Statement> assertionStatements = new LinkedList<Statement>();
			List<Statement> annotationStatements = new LinkedList<Statement>();
			RepositoryResult<Statement> iter = conn.getStatements(null, null, null);
			
			// Separation of statements for binding classes and for assertions
			while(iter.hasNext()) {
				Statement s = iter.next();
				
				if(s.getPredicate().stringValue().contains("bindingClass")) {
					bindingStatements.add(s);
				} else if(s.getPredicate().stringValue().contains(Constants.ANNOTATION_NS)
						|| s.getObject().stringValue().contains(Constants.ANNOTATION_NS)) {
					annotationStatements.add(s);
				} else {
					assertionStatements.add(s);
				}
			}
			
			conn.clear();
			Resource assertG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#assertionGraph");
			Resource annG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#annotationGraph");
			conn.add(bindingStatements);
			conn.add(assertionStatements, assertG);
			conn.add(annotationStatements, annG);
			
			iter = conn.getStatements(null, null, null);
			// Write all the graphs
			while(iter.hasNext()) {
				writer.handleStatement(iter.next());
			}
			
			// Clean
			writer.endRDF();
			conn.clear();
			conn.close();
			
			// submit insertion task
			String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
					"/insert_context_assertion/";
			
			this.client.post(ctxCoord.getPort(), ctxCoord.getAddress(), route, new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					if(resp.statusCode() != 201) {
						System.err.println("CtxCoord returned error while inserting context assertion");
					}
				}
			}).end(baos.toString());
		}
		
	}
	
	public boolean isFinished() {
		synchronized(syncObj) {
			return isFinished;
		}
	}
	
	private void setFinished(boolean finished) {
		synchronized(syncObj) {
			isFinished = finished;
		}
	}
}
