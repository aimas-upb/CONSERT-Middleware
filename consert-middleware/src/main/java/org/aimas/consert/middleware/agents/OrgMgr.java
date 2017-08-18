package org.aimas.consert.middleware.agents;

import java.util.ArrayList;
import java.util.List;

import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.ManagerSpecification;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * CtxCoord agent implemented as a Vert.x server
 */
public class OrgMgr extends AbstractVerticle implements Agent {

	protected Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private Repository repo; // repository containing the RDF data

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where this agent is hosted
	
	private AgentAddress ctxCoord;
	private AgentAddress ctxQueryHandler;
	private List<AgentAddress> ctxSensors;
	private AgentAddress ctxUser;


	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();
		
		this.ctxSensors = new ArrayList<AgentAddress>();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterManagement(vertx, this);

		// Set configuration
		AgentSpecification orgMgrSpec = MiddlewareConfig.readAgentConfig(ManagerSpecification.class,
				"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#OrgMgrSpec");
		CMMAgentContainer container = orgMgrSpec.getAgentAddress().getAgentContainer();
		this.agentConfig = new AgentConfig(container.getContainerHost(), container.getContainerPort());
		this.host = "0.0.0.0";

		// Start server
		this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println("Started OrgMgr on port " + this.agentConfig.getPort() + " host "
								+ this.host);
					} else {
						System.out.println("Failed to start OrgMgr on port " + this.agentConfig.getPort() + " host "
								+ this.host);
					}
					
					future.complete();
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
	

	public AgentAddress getCtxCoord() {
		return ctxCoord;
	}

	public void setCtxCoord(AgentAddress ctxCoord) {
		this.ctxCoord = ctxCoord;
	}

	public AgentAddress getCtxQueryHandler() {
		return ctxQueryHandler;
	}

	public void setCtxQueryHandler(AgentAddress ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
	}

	public List<AgentAddress> getCtxSensors() {
		return ctxSensors;
	}

	public void addCtxSensor(AgentAddress ctxSensor) {
		this.ctxSensors.add(ctxSensor);
	}

	public AgentAddress getCtxUser() {
		return ctxUser;
	}

	public void setCtxUser(AgentAddress ctxUser) {
		this.ctxUser = ctxUser;
	}
}
