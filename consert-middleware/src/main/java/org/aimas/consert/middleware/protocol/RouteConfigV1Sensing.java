package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxSensor;

import io.vertx.ext.web.RoutingContext;

public class RouteConfigV1Sensing extends RouteConfigV1 {
	
	private CtxSensor ctxSensor;
	
	
	public RouteConfigV1Sensing(CtxSensor ctxSensor) {
		this.ctxSensor = ctxSensor;
	}
	
	
	/**
	 * PUT tasking command
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		// TODO
	}
}
