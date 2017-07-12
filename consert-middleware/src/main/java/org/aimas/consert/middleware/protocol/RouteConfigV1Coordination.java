package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionCapabilitySubscription;
import org.aimas.consert.middleware.model.RDFObject;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxCoord agent in version 1
 */
public class RouteConfigV1Coordination extends RouteConfigV1 {

	private CtxCoord ctxCoord; // the agent that can be accessed with the
								// defined routes

	public RouteConfigV1Coordination(CtxCoord ctxCoord) {
		this.ctxCoord = ctxCoord;
	}

	/**
	 * POST publish assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxAsserts(RoutingContext rtCtx) {

		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability",
				AssertionCapability.class);

		// Insertion in CtxCoord
		AssertionCapability ac = (AssertionCapability) entry.getValue();
		this.ctxCoord.addAssertionCapability(entry.getKey(), ac);
	}

	/**
	 * GET list assertion capabilities
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAsserts(RoutingContext rtCtx) {

		// Get agent identifier from query
		String agent = rtCtx.request().getParam("agentIdentifier");

		// Get all known AssertionCapabilities
		Collection<AssertionCapability> acs = this.ctxCoord.getAssertionCapabilitiesValues();

		// Connection to repository to get the provider of each
		// AssertionCapability and the statements
		RepositoryConnection conn = this.ctxCoord.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		// Prepare to write RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();

		// For each AssertionCapability from CtxCoord, fetch the AgentSpec
		// provider.
		// If the provider is the requested one, write all the statements where
		// the AssertionCapability is the subject.
		for (AssertionCapability ac : acs) {

			// Check the provider
			AgentSpec as = ac.getProvider();

			if (as.getIdentifier().equals(agent)) {

				try {

					// Get all the statements corresponding to the
					// AssertionCapability (as the subject)
					Resource acRes = manager.getResource(ac.getId(), AssertionCapability.class);

					RepositoryResult<Statement> iter = conn.getStatements(acRes, null, null);

					// Write all the statements
					while (iter.hasNext()) {
						writer.handleStatement(iter.next());
					}

				} catch (RepositoryException | RDFBeanException e) {

					System.err.println("Error while getting information for AssertionCapability " + ac.getId());
					e.printStackTrace();
					rtCtx.response().setStatusCode(500).end();
				}
			}
		}

		conn.close();
		writer.endRDF();

		// Answer with the RDF statements
		rtCtx.response().setStatusCode(200).putHeader("content-type", "text/turtle").end(baos.toString());
	}

	/**
	 * GET list assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAssert(RoutingContext rtCtx) {

		// Get UUID of the AssertionCapability from query
		UUID uuid = UUID.fromString(rtCtx.request().getParam("id"));

		// Get the corresponding AssertionCapability
		AssertionCapability ac = this.ctxCoord.getAssertionCapability(uuid);

		this.get(rtCtx, AssertionCapability.class, ac);
	}

	/**
	 * PUT update assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxAssert(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");
		String resourceId = this.ctxCoord.getAssertionCapability(UUID.fromString(uuid)).getId();

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability",
				AssertionCapability.class, resourceId);

		if (entry != null) {

			// Remove old AssertionCapability from CtxCoord
			AssertionCapability ac = this.ctxCoord.removeAssertionCapability(UUID.fromString(uuid));
			if (ac == null) {
				rtCtx.response().setStatusCode(404).end();
				return;
			}

			AssertionCapability newAc = (AssertionCapability) entry.getValue();

			// Insertion in CtxCoord
			this.ctxCoord.addAssertionCapability(UUID.fromString(uuid), newAc);
		}
	}

	/**
	 * DELETE delete assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCtxAssert(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");

		// Remove old AssertionCapability from the repository
		String resourceId = this.ctxCoord.getAssertionCapability(UUID.fromString(uuid)).getId();

		boolean done = this.delete(rtCtx, AssertionCapability.class, resourceId);

		if (done) {

			// Remove old AssertionCapability from CtxCoord
			AssertionCapability ac = this.ctxCoord.removeAssertionCapability(UUID.fromString(uuid));
			if (ac == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}

	/**
	 * POST subscribe for assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostAssertCapSubs(RoutingContext rtCtx) {

		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription",
				AssertionCapabilitySubscription.class);

		// Insertion in CtxCoord
		AssertionCapabilitySubscription acs = (AssertionCapabilitySubscription) entry.getValue();
		this.ctxCoord.addAssertionCapabilitySubscription(entry.getKey(), acs);
	}

	/**
	 * GET inspect assertion capability subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetAssertCapSub(RoutingContext rtCtx) {

		// Get UUID of the object from query
		UUID uuid = UUID.fromString(rtCtx.request().getParam("id"));

		// Get the corresponding object
		AssertionCapabilitySubscription acs = this.ctxCoord.getAssertionCapabilitySubscription(uuid);

		this.get(rtCtx, AssertionCapabilitySubscription.class, acs);
	}

	/**
	 * PUT update assertion capability subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutAssertCapSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");
		String resourceId = this.ctxCoord.getAssertionCapabilitySubscription(UUID.fromString(uuid)).getId();

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription",
				AssertionCapabilitySubscription.class, resourceId);

		if (entry != null) {

			// Remove old AssertionCapabilitySubscription from CtxCoord
			AssertionCapabilitySubscription acs = this.ctxCoord
					.removeAssertionCapabilitySubscription(UUID.fromString(uuid));

			if (acs == null) {
				rtCtx.response().setStatusCode(404).end();
				return;
			}

			AssertionCapabilitySubscription newAc = (AssertionCapabilitySubscription) entry.getValue();

			// Insertion in CtxCoord
			this.ctxCoord.addAssertionCapabilitySubscription(UUID.fromString(uuid), newAc);
		}
	}

	/**
	 * DELETE unsubscribe for assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCapSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");

		// Remove old AssertionCapability from the repository
		String resourceId = this.ctxCoord.getAssertionCapabilitySubscription(UUID.fromString(uuid)).getId();

		boolean done = this.delete(rtCtx, AssertionCapabilitySubscription.class, resourceId);

		if (done) {

			// Remove old AssertionCapabilitySubscription from CtxCoord
			AssertionCapabilitySubscription acs = this.ctxCoord
					.removeAssertionCapabilitySubscription(UUID.fromString(uuid));
			if (acs == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}

	/**
	 * POST create ContextAssertion instance
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostInsCtxAssert(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST static context insertion
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostInsEntityDescs(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST static context update
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostUpdateEntDescs(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST activate ContextAssertionInstance
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostActivateCtxAssert(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST register query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostRegQueryHandler(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST unregister query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostUnregQueryHandler(RoutingContext rtCtx) {
		// TODO
	}

	private Entry<UUID, Object> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass) {
		return RouteUtils.post(rtCtx, rdfClassName, javaClass, this.ctxCoord);
	}

	private void get(RoutingContext rtCtx, Class<?> javaClass, RDFObject obj) {
		RouteUtils.get(rtCtx, javaClass, this.ctxCoord, obj);
	}

	private Entry<UUID, Object> put(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass, String resourceId) {
		return RouteUtils.put(rtCtx, rdfClassName, javaClass, this.ctxCoord, resourceId);
	}

	private boolean delete(RoutingContext rtCtx, Class<?> javaClass, String resourceId) {
		return RouteUtils.delete(rtCtx, javaClass, this.ctxCoord, resourceId);
	}
}
