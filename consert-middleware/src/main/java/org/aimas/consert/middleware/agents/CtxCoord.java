package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.CoordinatorSpecification;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionCapabilitySubscription;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * CtxCoord agent implemented as a Vert.x server
 */
public class CtxCoord extends AbstractVerticle implements Agent {

	protected Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted

	private Repository repo; // repository containing the RDF data

	protected Map<UUID, AssertionCapability> assertionCapabilities; // list of assertion capabilities
	private Map<UUID, AssertionCapabilitySubscription> assertionCapabilitySubscriptions; // list of subscriptions
																						 // for assertion capabilities

	private List<AgentAddress> ctxSensors;  // configuration to communicate with the CtxSensor agents
	private AgentAddress ctxUser;           // configuration to communicate with the CtxUser agent
	private AgentAddress orgMgr;            // configuration to communicate with the OrgMgr agent
	private AgentAddress consertEngine;     // configuration to communicate with the CONSERT Engine
	
	
	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repositories
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Initialization of the lists
		this.assertionCapabilities = new HashMap<UUID, AssertionCapability>();
		this.assertionCapabilitySubscriptions = new HashMap<UUID, AssertionCapabilitySubscription>();
		this.ctxSensors = new ArrayList<AgentAddress>();

		// Get configuration
		Future<Void> futureConfig = Future.future();
		futureConfig.setHandler(handler -> {
			
			// Set CONSERT Engine configuration
			this.consertEngine = new AgentAddress("127.0.0.1", 80);

			// Create router
			RouteConfig routeConfig = new RouteConfigV1();
			this.router = routeConfig.createRouterCoordination(this.vertx, this);

			// Start server
			this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
					res -> {
						if (res.succeeded()) {
							System.out.println("Started CtxCoord on port " + this.agentConfig.getPort() + " host "
								+ this.host);
						} else {
							System.out.println("Failed to start CtxCoord on port " + this.agentConfig.getPort()
								+ " host " + this.host);
						}					

						// Start CONSERT Engine
						DeploymentOptions engineDeplOpt = new DeploymentOptions();
						engineDeplOpt.setWorker(true);
						JsonObject engineConfig = new JsonObject()
								.put("address", this.consertEngine.getIpAddress())
								.put("port", this.consertEngine.getPort())
								.put("host", "0.0.0.0");
						engineDeplOpt.setConfig(engineConfig);
						
						this.vertx.deployVerticle(ConsertEngine.class.getName(), engineDeplOpt,
								result -> {
									
							future.complete();
						});
					});
		});
		
		// Get configuration of OrgMgr
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(CoordinatorSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#CtxCoordSpec");
		
		if(orgMgrSpec != null) {
			CMMAgentContainer container = orgMgrSpec.getAgentAddress().getAgentContainer();
			this.orgMgr = new AgentAddress(container.getContainerHost(), container.getContainerPort());
		} else {
			// use a default value
			this.orgMgr = new AgentAddress("127.0.0.1", 8080);
		}

		this.host = "0.0.0.0";
		this.getConfigFromOrgMgr(futureConfig);
	}
	
	public void stopVertx() {
		this.vertx.close();
	}

	@Override
	public void stop() {
		this.repo.shutDown();
	}

	@Override
	public Repository getRepository() {
		return this.repo;
	}
	
	
	private void getConfigFromOrgMgr(Future<Void> future) {

		final String rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		final String registerRoute = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE
				+ "/context_agents/";
		
		this.agentConfig = new AgentConfig();
		
		// Prepare a repository to convert the received RDF statements in a Java object
		Repository convRepo = new SailRepository(new MemoryStore());
		convRepo.initialize();
		RepositoryConnection convRepoConn = convRepo.getConnection();
		RDFBeanManager convManager = new RDFBeanManager(convRepoConn);
		
		HttpClient client = this.vertx.createHttpClient();
		
		// Query the OrgMgr agent to get the configuration to use
		client.post(orgMgr.getPort(), orgMgr.getIpAddress(), registerRoute, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						try {
							
							// Convert the statements to an object
							Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "", RDFFormat.TURTLE);
							convRepoConn.add(model);
							
							for(Statement s : model) {

								if(s.getPredicate().stringValue().contains(rdfType)) {
									
									AgentAddress addr = convManager.get(s.getSubject(), AgentAddress.class);
									agentConfig.setAddress(addr.getIpAddress());
									agentConfig.setPort(addr.getPort());
									break;
								}
							}
							
						} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
							System.err.println("Error while getting configuration for CtxCoord: " + e.getMessage());
							e.printStackTrace();
						}
						
						convRepoConn.close();
						convRepo.shutDown();
						
						future.complete();
					}
				});
			}
			
		}).putHeader("content-type", "text/plain").end("CtxCoord");
	}
	

	public AssertionCapability addAssertionCapability(UUID uuid, AssertionCapability ac) {
		return this.assertionCapabilities.put(uuid, ac);
	}

	public AssertionCapability getAssertionCapability(UUID uuid) {
		return this.assertionCapabilities.get(uuid);
	}

	public Collection<AssertionCapability> getAssertionCapabilitiesValues() {
		return this.assertionCapabilities.values();
	}

	public AssertionCapability removeAssertionCapability(UUID uuid) {
		return this.assertionCapabilities.remove(uuid);
	}

	public AssertionCapabilitySubscription addAssertionCapabilitySubscription(UUID uuid,
			AssertionCapabilitySubscription acs) {
		return this.assertionCapabilitySubscriptions.put(uuid, acs);
	}

	public AssertionCapabilitySubscription getAssertionCapabilitySubscription(UUID uuid) {
		return this.assertionCapabilitySubscriptions.get(uuid);
	}

	public AssertionCapabilitySubscription removeAssertionCapabilitySubscription(UUID uuid) {
		return this.assertionCapabilitySubscriptions.remove(uuid);
	}
	
	public AgentAddress getConsertEngineConfig() {
		return this.consertEngine;
	}
	
	public void addCtxSensor(AgentAddress ctxSensor) {
		this.ctxSensors.add(ctxSensor);
	}
	
	public boolean removeCtxSensor(AgentAddress ctxSensor) {
		return this.ctxSensors.remove(ctxSensor);
	}
}
