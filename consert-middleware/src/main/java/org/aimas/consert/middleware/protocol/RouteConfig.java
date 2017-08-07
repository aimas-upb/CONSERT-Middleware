package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.ConsertEngine;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.agents.CtxSensor;
import org.aimas.consert.middleware.agents.CtxUser;
import org.aimas.consert.middleware.agents.OrgMgr;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

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
	public static final String ENGINE_ROUTE = "/engine";

	/**
	 * Creates a router configured to be used on a CtxSensor agent
	 * 
	 * @param vertx the Vertx instance
	 * @param ctxSensor the CtxSensor agent related to these routes
	 * @return the created router
	 */
	public abstract Router createRouterSensing(Vertx vertx, CtxSensor ctxSensor);

	/**
	 * Creates a router configured to be used on a CtxUseragent
	 * 
	 * @param vertx the Vertx instance
	 * @param ctxUser the CtxUser agent related to these routes
	 * @return the created router
	 */
	public abstract Router createRouterUsage(Vertx vertx, CtxUser ctxUser);

	/**
	 * Creates a router configured to be used on a CtxCoord agent
	 * 
	 * @param vertx the Vertx instance
	 * @param ctxCoord the CtxCoord agent related to these routes
	 * @return the created router
	 */
	public abstract Router createRouterCoordination(Vertx vertx, CtxCoord ctxCoord);

	/**
	 * Creates a router configured to be used on a CtxQueryHandler agent
	 * 
	 * @param vertx the Vertx instance
	 * @param ctxQueryHandler the CtxQueryHandler agent related to this route
	 * @return the created router
	 */
	public abstract Router createRouterDissemination(Vertx vertx, CtxQueryHandler ctxQueryHandler);

	/**
	 * Creates a router configured to be used on an OrgMgr agent
	 * 
	 * @param vertx the Vertx instance
	 * @param orgNgr the OrgMgr agent related to this route
	 * @return the created router
	 */
	public abstract Router createRouterManagement(Vertx vertx, OrgMgr orgMgr);

	public abstract Router createRouterEngine(Vertx vertx, ConsertEngine consertEngine);

	/**
	 * Creates a new router with no route
	 * 
	 * @param vertx the Vertx instance
	 * @return the created router
	 */
	protected Router createRouter(Vertx vertx) {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		return router;
	}
}
