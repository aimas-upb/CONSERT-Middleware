package org.aimas.consert.middleware.agents;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aimas.consert.engine.EngineRunner;
import org.aimas.consert.engine.EventTracker;
import org.aimas.consert.engine.api.ContextAssertionListener;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.TestSetup;
import org.aimas.consert.utils.PlotlyExporter;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
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
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kie.api.runtime.KieSession;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * CONSERT Engine implemented as a Vert.x server
 */
public class ConsertEngine extends AbstractVerticle implements Agent, ContextAssertionListener {

	protected Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data
	private RepositoryConnection repoConn;  // connection to the repository
	private RDFBeanManager manager;  // manager for the repository
	
	private Thread engineRunner;               // thread to run the CONSERT Engine
	private EventTracker eventTracker;         // service that allows to add events in the engine
	private KieSession kSession;               // rules for the engine
	private ExecutorService insertionService;  // service allowing to insert context assertions in the CONSERT Engine
	
	
	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		this.repoConn = this.repo.getConnection();
		this.manager = new RDFBeanManager(this.repoConn);

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterEngine(this.vertx, this);

		// Get configuration
		JsonObject config = this.config();
		this.agentConfig = new AgentConfig(config.getString("address"), config.getInteger("port"));
		this.host = config.getString("host");

		// Start server
		this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println("Started CONSERT Engine on port " + this.agentConfig.getPort() + " host "
								+ this.host);
					} else {
						System.out.println("Failed to start CONSERT Engine on port " + this.agentConfig.getPort()
							+ " host " + this.host);
					}					

					// Start CONSERT Engine
					this.kSession = TestSetup.getKieSessionFromResources("rules/HLA.drl");
			    	this.engineRunner = new Thread(new EngineRunner(kSession));
			    	this.eventTracker = new EventTracker(kSession);
			    	this.eventTracker.addEventListener(this);
			    	this.insertionService = Executors.newSingleThreadExecutor();
			    	
			    	this.engineRunner.start();
					future.complete();
				});
	}
	
	public void stopVertx() {
		this.vertx.close();
	}

	@Override
	public void stop() {
		
		//dumpRepository("dump.txt");
		
		this.repoConn.close();
		this.repo.shutDown();

    	PlotlyExporter.exportToHTML(null, this.kSession);
    	this.insertionService.shutdownNow();
    	this.kSession.halt();
    	this.kSession.dispose();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}

	@Override
	public void notifyAssertionInserted(ContextAssertion assertion) {
		try {
			manager.add(assertion);
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while inserting assertion in engine's repository: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void notifyAssertionDeleted(ContextAssertion assertion) {
		try {
			manager.delete(assertion.getAssertionIdentifier(), ContextAssertion.class);
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while deleting assertion from engine's repository: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void insertEvent(ContextAssertion ca) {
		this.insertionService.execute(new EventInsertionTask(ca));
	}
	
	/**
	 * Allows to insert an event in the engine
	 */
	private class EventInsertionTask implements Runnable {
		private ContextAssertion ca;
		
		EventInsertionTask(ContextAssertion ca) {
			this.ca = ca;
		}
		
		public void run() {
			eventTracker.insertAtomicEvent(ca);
        }
	}
	
	/**
	 * Write the content of the repository to a file
	 * @param filename name of the file to write
	 */
	private void dumpRepository(String filename) {
		
		List<IRI> iris = new ArrayList<IRI>();
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/WorkingHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/ExerciseHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/DiscussingHLA"));
		iris.add(SimpleValueFactory.getInstance().createIRI("http://example.org/hlatest/DiningHLA"));
		
		try {
			Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, fileWriter);
			
			writer.startRDF();
			
			for(IRI iri : iris) {
				RepositoryResult<Statement> statements = this.repoConn.getStatements(null, null, iri);
				while(statements.hasNext()) {
					writer.handleStatement(statements.next());
				}
			}
			
			writer.endRDF();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
