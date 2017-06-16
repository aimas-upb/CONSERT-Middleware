package org.aimas.consert.middleware.protocol;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class RouteConfigV1 extends RouteConfig {

	@Override
	public Router createRouterSensing(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.put(RouteConfig.API_ROUTE + RouteConfig.SENSING_ROUTE + "/tasking_command/").handler(null);
		
		return router;
	}

	@Override
	public Router createRouterUsage(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.put(RouteConfig.API_ROUTE + RouteConfig.USAGE_ROUTE + "/tasking_command/").handler(null);
		
		return router;
	}

	@Override
	public Router createRouterCoordination(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/context_assertions/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/context_assertions/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/context_assertions/:id/").handler(null);
		router.put(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/context_assertions/:id/").handler(null);
		router.delete(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/context_assertions/:id/").handler(null);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/assertion_capability_subscriptions/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/assertion_capability_subscriptions/:id/").handler(null);
		router.put(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/assertion_capability_subscriptions/:id/").handler(null);
		router.delete(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/assertion_capability_subscriptions/:id/").handler(null);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/insert_context_assertion/").handler(null);
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/insert_entity_descriptions/").handler(null);
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/update_entity_descriptions/").handler(null);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/activate_context_assertion/").handler(null);
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/register_query_handler/").handler(null);
		router.post(RouteConfig.API_ROUTE + RouteConfig.COORDINATION_ROUTE + "/unregister_query_handler/").handler(null);
		
		return router;
	}

	@Override
	public Router createRouterDissemination(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/unregister_query_user/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_query/").handler(null);
		router.post(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_subscriptions/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_subscriptions/:id/").handler(null);
		router.put(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_subscriptions/:id/").handler(null);
		router.delete(RouteConfig.API_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_subscriptions/:id/").handler(null);
		
		return router;
	}

	@Override
	public Router createRouterManagement(Vertx vertx) {
		
		Router router = this.createRouter(vertx);
		
		router.post(RouteConfig.API_ROUTE + RouteConfig.MANAGEMENT_ROUTE + "/context_agents/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.MANAGEMENT_ROUTE + "/find_coordinator/").handler(null);
		router.get(RouteConfig.API_ROUTE + RouteConfig.MANAGEMENT_ROUTE + "/find_query_handler/").handler(null);
		
		return router;
	}

}
