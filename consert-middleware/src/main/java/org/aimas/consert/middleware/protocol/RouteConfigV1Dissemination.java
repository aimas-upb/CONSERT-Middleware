package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxQueryHandler;

import io.vertx.ext.web.RoutingContext;

public class RouteConfigV1Dissemination extends RouteConfigV1 {
	
	private CtxQueryHandler ctxQueryHandler;
	
	
	public RouteConfigV1Dissemination(CtxQueryHandler ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
	}
	
	
	/**
	 * POST register query user
	 * @param rtCtx the routing context
	 */
	public void handlePostUnregQueryUser(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET query context
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxQuery(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST subscribe for context
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxSubs(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET inspect context subscription
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * PUT update context subscription
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * DELETE unsubscribe for context
	 * @param rtCtx
	 */
	public void handleDeleteCtxSub(RoutingContext rtCtx) {
		// TODO
	}
}
