package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxCoord;

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
		// TODO
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
