package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxCoord agent in version 1
 */
public class RouteConfigV1Coordination extends RouteConfigV1 {
	
	private CtxCoord ctxCoord;  // the agent that can be accessed with the defined routes
	
	
	public RouteConfigV1Coordination(CtxCoord ctxCoord) {
		this.ctxCoord = ctxCoord;
	}
	
	
	/**
	 * POST publish assertion capability
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxAsserts(RoutingContext rtCtx) {
		
		// Initialization
		String rdf = rtCtx.getBodyAsString();
		UUID uuid = UUID.randomUUID();
		
		RepositoryConnection conn = this.ctxCoord.getRepo().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			// Getting the object we just inserted
			for(Statement s : model) {
				if(s.getObject().stringValue().equals("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol"
						+ "#AssertionCapability")) {
					
					AssertionCapability ac = manager.get(s.getSubject(), AssertionCapability.class);
					
					// Insertion in CtxCoord
					this.ctxCoord.addAssertionCapability(uuid, ac);
					
					break;
				}
			}
			
			// Answer by giving the UUID associated to the AssertionCapability
			rtCtx.request().response().putHeader("content-type", "text/plain")
				.end(uuid.toString());
		} catch (RDF4JException | RDFBeanException | IOException e) {
			System.err.println("Error while creating new AssertionCapability: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * GET list assertion capabilities
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAsserts(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET list assertion capability
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAssert(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * PUT update assertion capability
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxAssert(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * DELETE delete assertion capability
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCtxAssert(RoutingContext rtCtx) {
		// TODO
	}
	
	
	/**
	 * POST subscribe for assertion capability
	 * @param rtCtx the routing context
	 */
	public void handlePostAssertCapSubs(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET inspect assertion capability subscription
	 * @param rtCtx the routing context
	 */
	public void handleGetAssertCapSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * PUT update assertion capability subscription
	 * @param rtCtx the routing context
	 */
	public void handlePutAssertCapSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * DELETE unsubscribe for assertion capability
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCapSub(RoutingContext rtCtx) {
		// TODO
	}
	
	
	/**
	 * POST create ContextAssertion instance
	 * @param rtCtx the routing context
	 */
	public void handlePostInsCtxAssert(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST static context insertion
	 * @param rtCtx the routing context
	 */
	public void handlePostInsEntityDescs(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST static context update
	 * @param rtCtx the routing context
	 */
	public void handlePostUpdateEntDescs(RoutingContext rtCtx) {
		// TODO
	}
	

	/**
	 * POST activate ContextAssertionInstance
	 * @param rtCtx the routing context
	 */
	public void handlePostActivateCtxAssert(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST register query handler
	 * @param rtCtx the routing context
	 */
	public void handlePostRegQueryHandler(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST unregister query handler
	 * @param rtCtx the routing context
	 */
	public void handlePostUnregQueryHandler(RoutingContext rtCtx) {
		// TODO
	}
}
