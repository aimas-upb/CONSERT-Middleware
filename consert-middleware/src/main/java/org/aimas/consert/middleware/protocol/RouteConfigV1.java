package org.aimas.consert.middleware.protocol;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class RouteConfigV1 extends RouteConfig {
	
	public static final String VERSION_ROUTE = "/v1";

	@Override
	public Router createRouterSensing(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.SENSING_ROUTE +
			"/tasking_command/").handler(RouteConfigV1Sensing::handlePutTaskingCommand);
		
		return router;
	}

	@Override
	public Router createRouterUsage(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.USAGE_ROUTE +
			"/tasking_command/").handler(RouteConfigV1Usage::handlePutTaskingCommand);
		
		return router;
	}

	@Override
	public Router createRouterCoordination(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/context_assertions/").handler(RouteConfigV1Coordination::handlePostCtxAsserts);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/context_assertions/").handler(RouteConfigV1Coordination::handleGetCtxAsserts);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/context_assertions/:id/").handler(RouteConfigV1Coordination::handleGetCtxAssert);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/context_assertions/:id/").handler(RouteConfigV1Coordination::handlePutCtxAssert);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/context_assertions/:id/").handler(RouteConfigV1Coordination::handleDeleteCtxAssert);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/assertion_capability_subscriptions/").handler(RouteConfigV1Coordination::handlePostAssertCapSubs);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/assertion_capability_subscriptions/:id/").handler(RouteConfigV1Coordination::handleGetAssertCapSub);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/assertion_capability_subscriptions/:id/").handler(RouteConfigV1Coordination::handlePutAssertCapSub);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/assertion_capability_subscriptions/:id/").handler(RouteConfigV1Coordination::handleDeleteCapSub);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/insert_context_assertion/").handler(RouteConfigV1Coordination::handlePostInsCtxAssert);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/insert_entity_descriptions/").handler(RouteConfigV1Coordination::handlePostInsEntityDescs);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/update_entity_descriptions/").handler(RouteConfigV1Coordination::handlePostUpdateEntDescs);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/activate_context_assertion/").handler(RouteConfigV1Coordination::handlePostActivateCtxAssert);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/register_query_handler/").handler(RouteConfigV1Coordination::handlePostRegQueryHandler);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE +
			"/unregister_query_handler/").handler(RouteConfigV1Coordination::handlePostUnregQueryHandler);
		
		return router;
	}

	@Override
	public Router createRouterDissemination(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/unregister_query_user/").handler(RouteConfigV1Dissemination::handlePostUnregQueryUser);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/context_query/").handler(RouteConfigV1Dissemination::handleGetCtxQuery);
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/context_subscriptions/").handler(RouteConfigV1Dissemination::handlePostCtxSubs);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/context_subscriptions/:id/").handler(RouteConfigV1Dissemination::handleGetCtxSub);
		router.put(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/context_subscriptions/:id/").handler(RouteConfigV1Dissemination::handlePutCtxSub);
		router.delete(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE +
			"/context_subscriptions/:id/").handler(RouteConfigV1Dissemination::handleDeleteCtxSub);
		
		return router;
	}

	@Override
	public Router createRouterManagement(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE +
			"/context_agents/").handler(RouteConfigV1Management::handlePostCtxAgents);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE +
			"/find_coordinator/").handler(RouteConfigV1Management::handleGetFindCoord);
		router.get(RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.MANAGEMENT_ROUTE +
			"/find_query_handler/").handler(RouteConfigV1Management::handleGetFindQueryHandler);
		
		return router;
	}

}
