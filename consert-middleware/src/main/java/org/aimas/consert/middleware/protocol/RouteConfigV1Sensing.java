package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.CtxSensor;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxSensor agent in version 1
 */
public class RouteConfigV1Sensing extends RouteConfigV1 {

	private CtxSensor ctxSensor; // the agent that can be accessed with the
									// defined routes

	public RouteConfigV1Sensing(CtxSensor ctxSensor) {
		this.ctxSensor = ctxSensor;
	}

	/**
	 * PUT tasking command
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		// TODO
	}
}
