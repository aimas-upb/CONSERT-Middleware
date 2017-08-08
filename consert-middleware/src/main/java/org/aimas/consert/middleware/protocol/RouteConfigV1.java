package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.agents.ConsertEngine;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.agents.CtxSensor;
import org.aimas.consert.middleware.agents.CtxUser;
import org.aimas.consert.middleware.agents.OrgMgr;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Defines the configuration of a router for version 1
 */
public class RouteConfigV1 extends RouteConfig {

	public static final String VERSION_ROUTE = "/v1";

	@Override
	public Router createRouterSensing(Vertx vertx, CtxSensor ctxSensor) {

		Router router = this.createRouter(vertx);
		RouteConfigV1Sensing sensing = new RouteConfigV1Sensing(ctxSensor);

		router.put(
				RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.SENSING_ROUTE + "/tasking_command/")
				.handler(sensing::handlePutTaskingCommand);

		return router;
	}

	@Override
	public Router createRouterUsage(Vertx vertx, CtxUser ctxUser) {

		Router router = this.createRouter(vertx);
		RouteConfigV1Usage usage = new RouteConfigV1Usage(ctxUser);

		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.USAGE_ROUTE + "/tasking_command/")
				.handler(usage::handlePutTaskingCommand);
		
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/subscription_update/:id/").handler(usage::handleSubsUpdate);

		return router;
	}

	@Override
	public Router createRouterCoordination(Vertx vertx, CtxCoord ctxCoord) {

		Router router = this.createRouter(vertx);
		RouteConfigV1Coordination coordination = new RouteConfigV1Coordination(ctxCoord);

		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/context_assertions/").handler(coordination::handlePostCtxAsserts);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/context_assertions/").handler(coordination::handleGetCtxAsserts);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/context_assertions/:id/").handler(coordination::handleGetCtxAssert);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/context_assertions/:id/").handler(coordination::handlePutCtxAssert);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/context_assertions/:id/").handler(coordination::handleDeleteCtxAssert);

		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/assertion_capability_subscriptions/").handler(coordination::handlePostAssertCapSubs);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/assertion_capability_subscriptions/:id/").handler(coordination::handleGetAssertCapSub);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/assertion_capability_subscriptions/:id/").handler(coordination::handlePutAssertCapSub);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/assertion_capability_subscriptions/:id/").handler(coordination::handleDeleteCapSub);

		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/insert_context_assertion/").handler(coordination::handlePostInsCtxAssert);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/insert_entity_descriptions/").handler(coordination::handlePostInsEntityDescs);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/update_entity_descriptions/").handler(coordination::handlePostUpdateEntDescs);

		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/activate_context_assertion/").handler(coordination::handlePostActivateCtxAssert);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/register_query_handler/").handler(coordination::handlePostRegQueryHandler);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE
				+ "/unregister_query_handler/").handler(coordination::handlePostUnregQueryHandler);

		return router;
	}

	@Override
	public Router createRouterDissemination(Vertx vertx, CtxQueryHandler ctxQueryHandler) {

		Router router = this.createRouter(vertx);
		RouteConfigV1Dissemination dissemination = new RouteConfigV1Dissemination(ctxQueryHandler);

		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/register_query_user/").handler(dissemination::handlePostRegQueryUser);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/context_query/").handler(dissemination::handleGetCtxQuery);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/context_subscriptions/").handler(dissemination::handlePostCtxSubs);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/context_subscriptions/:id/").handler(dissemination::handleGetCtxSub);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/context_subscriptions/:id/").handler(dissemination::handlePutCtxSub);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/context_subscriptions/:id/").handler(dissemination::handleDeleteCtxSub);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/resources/:id/").handler(dissemination::handleResources);
		
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
				+ "/subscription_update/:id/").handler(dissemination::handleSubsUpdate);

		return router;
	}

	@Override
	public Router createRouterManagement(Vertx vertx, OrgMgr orgMgr) {

		Router router = this.createRouter(vertx);
		RouteConfigV1Management management = new RouteConfigV1Management(orgMgr);

		router.post(
				RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE + "/context_agents/")
				.handler(management::handlePostCtxAgents);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE
				+ "/find_coordinator/").handler(management::handleGetFindCoord);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE
				+ "/find_query_handler/").handler(management::handleGetFindQueryHandler);

		return router;
	}

	@Override
	public Router createRouterEngine(Vertx vertx, ConsertEngine consertEngine) {
		
		Router router = this.createRouter(vertx);
		RouteConfigV1Engine engine = new RouteConfigV1Engine(consertEngine);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.ENGINE_ROUTE + "/insert_event/")
			.handler(engine::handleInsertEvent);
		
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.ENGINE_ROUTE + "/answer_query/")
			.handler(engine::handleGetAnswerQuery);
		
		return router;
	}
}
