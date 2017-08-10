package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.OrgMgr;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for an OrgMgr agent in version 1
 */
public class RouteConfigV1Management extends RouteConfigV1 {

	private OrgMgr orgMgr; // the agent that can be accessed with the defined routes

	public RouteConfigV1Management(OrgMgr orgMgr) {
		this.orgMgr = orgMgr;
	}

	/**
	 * POST register agent
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxAgents(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * GET find coordinator
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetFindCoord(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * GET find query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetFindQueryHandler(RoutingContext rtCtx) {
		// TODO
	}
}
