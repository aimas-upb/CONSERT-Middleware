package org.aimas.consert.middleware.protocol;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Defines the configuration of a router
 */
public abstract class RouteConfig {

	public static final String API_ROUTE = "/api";
	public static final String SENSING_ROUTE = "/sensing";
	public static final String USAGE_ROUTE = "/usage";
	public static final String COORDINATION_ROUTE = "/coordination";
	public static final String DISSEMINATION_ROUTE = "/dissemination";
	public static final String MANAGEMENT_ROUTE = "/management";
	
	
	/**
	 * Creates a router configured to be used on a CtxSensor agent
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	public abstract Router createRouterSensing(Vertx vertx);
	
	/**
	 * Creates a router configured to be used on a CtxUseragent
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	public abstract Router createRouterUsage(Vertx vertx);
	
	/**
	 * Creates a router configured to be used on a CtxCoordagent
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	public abstract Router createRouterCoordination(Vertx vertx);
	
	/**
	 * Creates a router configured to be used on a CtxQueryHandleragent
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	public abstract Router createRouterDissemination(Vertx vertx);
	
	/**
	 * Creates a router configured to be used on an OrgMgr agent
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	public abstract Router createRouterManagement(Vertx vertx);
	
	/**
	 * Creates a new router with no route
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	protected Router createRouter(Vertx vertx) {
		return Router.router(vertx);
	}
}
