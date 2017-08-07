package org.aimas.consert.middleware.agents;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.aimas.consert.model.Constants;
import org.aimas.consert.model.content.ContextAssertion;
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
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * CtxSensor agent implemented as a Vert.x server
 */
public abstract class CtxSensor extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties"; // path to the configuration file for this agent
	
	private final String INSERT_CONTEXT_ASSERTION_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.COORDINATION_ROUTE + "/insert_context_assertion/";

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	protected AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted
	protected int id; // identifier to distinguish CtxSensor instances
	
	private boolean isFinished = false;     // allows to know when the CtxSensor has finished sending all the eventss

	private Repository repo; // repository containing the RDF data

	private AgentConfig ctxCoord; // configuration to communicate with the CtxCoord agent
	private AgentConfig orgMgr; // configuration to communicate with the OrgMgr agent
	
	private HttpClient client;  // an HTTP client that can be used to send requests
	
	private List<UUID> assertionCapabilitiesIds;  // identifiers of the sent assertion capabilities
	
	protected Map<URI, AssertionUpdateMode> updateModes;  // the update mode for all the enabled assertion types


	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		this.client = this.vertx.createHttpClient();
		this.updateModes = new HashMap<URI, AssertionUpdateMode>();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
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
		server.requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host, res -> {
			if (res.succeeded()) {
				System.out.println("Started CtxSensor on port " + server.actualPort() + " host " + this.host);
			} else {
				System.out.println("Failed to start CtxSensor on port " + server.actualPort() + " host " + this.host);
			}

			// Send the assertion capabilities before doing anything
			this.assertionCapabilitiesIds = new LinkedList<UUID>();
			this.sendAssertionCapabilities(future);
			
			// Read events from the adaptor and send them to the CtxCoord agent
			this.readEvents();
		});
	}

	@Override
	public void stop() {
		this.repo.shutDown();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}
	
	/**
	 * starts reading the events from the adaptor and sending them to the CtxCoord
	 */
	protected abstract void readEvents();

	/**
	 * send default assertion capability
	 * @param future allows the execution in async mode
	 */
	protected abstract void sendAssertionCapabilities(Future<Void> future);
	
	/**
	 * send the assertion capability to the CtxCoord
	 * @param acs the assertion capabilities to send
	 * @param future allows the execution in async mode
	 */
	protected void sendAssertionCapabilities(List<AssertionCapability> acs, Future<Void> future) {
		
		String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
				"/context_assertions/";
		
		// using a repository to convert the AssertionCapability objects to RDF statements
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection conn = repository.getConnection();
		
		RDFBeanManager manager = new RDFBeanManager(conn);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			
			// Convert each AssertionCapability one by one
			
			for(AssertionCapability ac : acs) {
			
				baos.reset();
				RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
				writer.startRDF();
				
				conn.clear();
				manager.add(ac);
				
				RepositoryResult<Statement> iter = conn.getStatements(null, null, null);
				
				while(iter.hasNext()) {
					writer.handleStatement(iter.next());
				}
				
				writer.endRDF();
				
				// send to the CtxCoord
				this.client.post(ctxCoord.getPort(), ctxCoord.getAddress(), route, new Handler<HttpClientResponse>() {
	
					@Override
					public void handle(HttpClientResponse resp) {
						
						if(resp.statusCode() == 201) {
							resp.bodyHandler(new Handler<Buffer>() {
	
								@Override
								public void handle(Buffer buffer) {
									assertionCapabilitiesIds.add(UUID.fromString(buffer.toString()));
									
									if(assertionCapabilitiesIds.size() == acs.size()) {
										future.complete();
									}
								}
	
							});
						}
					}
					
				}).putHeader("content-type", "text/turtle").end(baos.toString());
			}
			
		} catch (RepositoryException | RDFBeanException e) {
			e.printStackTrace();
		}
		
		conn.close();
		repository.shutDown();
	}
	
	/**
	 *  Delete all the assertion capabilities from this CtxSensor on the CtxCoord
	 */
	protected void deleteAssertionCapabilities() {
		
		String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
				"/context_assertions/";
		
		// send to the CtxCoord
		for(UUID uuid : this.assertionCapabilitiesIds) {
			
			this.client.delete(ctxCoord.getPort(), ctxCoord.getAddress(), route + uuid.toString() + "/",
					new Handler<HttpClientResponse>() {

				@Override
				public void handle(HttpClientResponse resp) {
					
					if(resp.statusCode() != 200) {
						System.err.println("Error while deleting assertion capability " + uuid.toString());
					}
				}
				
			}).end();
		}
		
		this.assertionCapabilitiesIds.clear();
	}
	
	/**
	 *  Sends the event to the known CtxCoord
	 * @param event the context assertion to send
	 */
	protected void sendEvent(ContextAssertion event) {

		System.out.println("CtxSensor " + id + " sends event " + event);
		
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
		
		// Write all the graphs
		iter = conn.getStatements(null, null, null);
		while(iter.hasNext()) {
			writer.handleStatement(iter.next());
		}
		
		// Clean
		writer.endRDF();
		conn.clear();
		conn.close();

		// submit insertion task
		this.client.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(), this.INSERT_CONTEXT_ASSERTION_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				if(resp.statusCode() != 201) {
					System.err.println("CtxCoord returned error while inserting context assertion");
				}
			}
		}).end(baos.toString());
	}
	
	public boolean isFinished() {
		return isFinished;
	}
	
	protected void setFinished(boolean finished) {
		isFinished = finished;
	}
	
	public void startUpdates(URI assertionType, AssertionUpdateMode updateMode) {
		this.updateModes.put(assertionType, updateMode);
	}
	
	public void stopUpdates(URI assertionType) {
		this.updateModes.remove(assertionType);
	}
	
	public void alterUpdates(URI assertionType, AssertionUpdateMode newUpdateMode) {
		this.updateModes.replace(assertionType, newUpdateMode);
	}
	
	public AgentConfig getAgentConfig() {
		return this.agentConfig;
	}
}
