package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxUser;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxUser agent in version 1
 */
public class RouteConfigV1Usage extends RouteConfigV1 {
	
	private CtxUser ctxUser;  // the agent that can be accessed with the defined routes
	
	
	public RouteConfigV1Usage(CtxUser ctxUser) {
		this.ctxUser = ctxUser;
	}
	
	
	/**
	 * PUT tasking command
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		// TODO
	}
}
