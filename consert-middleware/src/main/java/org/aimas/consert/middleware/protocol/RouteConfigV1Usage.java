package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxUser;

import io.vertx.ext.web.RoutingContext;

public class RouteConfigV1Usage extends RouteConfigV1 {
	
	private CtxUser ctxuser;
	
	
	public RouteConfigV1Usage(CtxUser ctxUser) {
		this.ctxuser = ctxUser;
	}
	
	
	/**
	 * PUT tasking command
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		// TODO
	}
}
