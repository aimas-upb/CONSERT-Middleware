package org.aimas.consert.middleware.agents;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.protocol.ContextSubscriptionResource;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * CtxQueryHandler agent implemented as a Vert.x server
 */
public class CtxQueryHandler extends AbstractVerticle implements Agent {

	private final String CONFIG_FILE = "agents.properties"; // path to the
															// configuration
															// file for this
															// agent

	private Vertx vertx; // Vertx instance
	private Router router; // router for communication with this agent

	private AgentConfig agentConfig; // configuration values for this agent
	private String host; // where the agent is hosted

	private Repository repo; // repository containing the RDF data

	public Map<UUID, ContextSubscriptionResource> contextSubscriptions; // list
																		// of
																		// context
																		// subscriptions

	private AgentConfig ctxCoord; // configuration to communicate with the
									// CtxCoord agent
	private AgentConfig orgMgr; // configuration to communicate with the OrgMgr
								// agent

	public static void main(String[] args) {

		// CtxQueryHandler.vertx.deployVerticle(CtxQueryHandler.class.getName());
	}

	@Override
	public void start(Future<Void> future) {

		this.vertx = this.context.owner();

		// Initialization of the repository
		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();

		// Initialization of the lists
		this.contextSubscriptions = new HashMap<UUID, ContextSubscriptionResource>();

		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterDissemination(this.vertx, this);

		// Read configuration
		try {

			Configuration config = new PropertiesConfiguration(CONFIG_FILE);

			this.agentConfig = AgentConfig.readCtxQueryHandlerConfig(config);
			this.host = config.getString("CtxQueryHandler.host");

			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);

		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}

		// Start server
		this.vertx.createHttpServer().requestHandler(router::accept).listen(this.agentConfig.getPort(), this.host,
				res -> {
					if (res.succeeded()) {
						System.out.println(
								"Started CtxQueryHandler on port " + this.agentConfig.getPort() + " host " + this.host);
					} else {
						System.out.println("Failed to start CtxQueryHandler server on port "
								+ this.agentConfig.getPort() + " host " + this.host);
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

	public void addContextSubscription(UUID key, ContextSubscriptionResource cs) {
		this.contextSubscriptions.put(key, cs);
	}

	public ContextSubscriptionResource getContextSubscription(UUID uuid) {
		return this.contextSubscriptions.get(uuid);
	}

	public String getContextSubscriptionRDF(UUID uuid) {

		ContextSubscription ctxSubs = this.contextSubscriptions.get(uuid).getContextSubscription();

		// Connection to repository to get all the statements
		RepositoryConnection conn = this.repo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		// Prepare to write RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();

		try {

			// Get all the statements corresponding to the given object (as the
			// subject)
			Resource objRes = manager.getResource(ctxSubs.getId(), ContextSubscription.class);

			RepositoryResult<Statement> iter = conn.getStatements(objRes, null, null);

			// Write all the statements
			while (iter.hasNext()) {

				writer.handleStatement(iter.next());
			}

			conn.close();

		} catch (RepositoryException | RDFBeanException e) {

			conn.close();
			System.err.println("Error while getting information for object " + ctxSubs.getId());
			e.printStackTrace();
		}

		writer.endRDF();

		return writer.toString();
	}

	public ContextSubscriptionResource removeContextSubscription(UUID uuid) {
		return this.contextSubscriptions.remove(uuid);
	}

	public AgentConfig getAgentConfig() {
		return agentConfig;
	}
}
