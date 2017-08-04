package org.aimas.consert.middleware.protocol;

import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.model.ContextSubscription;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxQueryHandler agent in version 1
 */
public class RouteConfigV1Dissemination extends RouteConfigV1 {

	private CtxQueryHandler ctxQueryHandler; // the agent that can be accessed with the defined routes

	public RouteConfigV1Dissemination(CtxQueryHandler ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
	}

	/**
	 * POST register query user
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostRegQueryUser(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * GET query context
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxQuery(RoutingContext rtCtx) {
		
		HttpClient client = this.ctxQueryHandler.getVertx().createHttpClient();
		
		AgentConfig ctxCoordConfig = this.ctxQueryHandler.getCtxCoordConfig();
		String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.COORDINATION_ROUTE + "/answer_query/";
		
		client.get(ctxCoordConfig.getPort(), ctxCoordConfig.getAddress(), route, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						rtCtx.response().setStatusCode(resp.statusCode()).putHeader("content-type", "text/turtle")
							.end(buffer.toString());
					}
				});
			}
		}).putHeader("content-type", "text/turtle").end(rtCtx.getBodyAsString());
	}

	/**
	 * POST subscribe for context
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxSubs(RoutingContext rtCtx) {

		RequestBean reqBean = Json.decodeValue(rtCtx.getBodyAsString(), RequestBean.class);
		rtCtx.setBody(Buffer.buffer(reqBean.getRequest()));

		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription",
				ContextSubscription.class);

		// Insertion in CtxQueryhandler
		ContextSubscription cs = (ContextSubscription) entry.getValue();

		// Create the resource
		AgentConfig ctxQHConfig = this.ctxQueryHandler.getAgentConfig();
		ContextSubscriptionResource ctxSubsRes = new ContextSubscriptionResource();
		ctxSubsRes.setResourceURI(ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort() + RouteConfig.API_ROUTE
				+ RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/context_subscriptions/"
				+ entry.getKey().toString());
		ctxSubsRes.setInitiatorURI(reqBean.getInitiatorURI());
		ctxSubsRes.setParticipantURI(ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort());
		ctxSubsRes.setRequest(reqBean.getRequest());
		ctxSubsRes.setState(RequestState.REQ_RECEIVED);
		ctxSubsRes.setInitiatorCallbackURI(reqBean.getInitiatorCallbackURI());
		ctxSubsRes.setContextSubscription(cs);

		// Add the resource in CtxQueryHandler
		this.ctxQueryHandler.addContextSubscription(entry.getKey(), ctxSubsRes);
	}

	/**
	 * GET inspect context subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxSub(RoutingContext rtCtx) {

		// Get resource
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		ContextSubscriptionResource resource = this.ctxQueryHandler.getContextSubscription(resourceUUID);

		// Send resource if found
		if (resource != null) {
			rtCtx.response().putHeader("content-type", "application/json").setStatusCode(200)
					.end(Json.encodePrettily(resource));
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}

	/**
	 * PUT update context subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");
		String resourceId = this.ctxQueryHandler.getContextSubscription(UUID.fromString(uuid)).getContextSubscription()
				.getId();

		RequestBean reqBean = Json.decodeValue(rtCtx.getBodyAsString(), RequestBean.class);
		rtCtx.setBody(Buffer.buffer(reqBean.getRequest()));

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription",
				ContextSubscription.class, resourceId);

		if (entry != null) {

			ContextSubscription newCs = (ContextSubscription) entry.getValue();

			// Insertion in CtxQueryHandler
			this.ctxQueryHandler.getContextSubscription(UUID.fromString(uuid)).setContextSubscription(newCs);
		}
	}

	/**
	 * DELETE unsubscribe for context
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCtxSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");

		// Remove old ContextSubscription from the repository
		String resourceId = this.ctxQueryHandler.getContextSubscription(UUID.fromString(uuid)).getContextSubscription()
				.getId();

		boolean done = this.delete(rtCtx, ContextSubscription.class, resourceId);

		if (done) {

			// Remove old ContextSubscription from CtxQueryHandler
			ContextSubscriptionResource cs = this.ctxQueryHandler.removeContextSubscription(UUID.fromString(uuid));
			if (cs == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}

	private Entry<UUID, Object> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass) {
		return RouteUtils.post(rtCtx, rdfClassName, javaClass, this.ctxQueryHandler);
	}

	private Entry<UUID, Object> put(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass, String resourceId) {
		return RouteUtils.put(rtCtx, rdfClassName, javaClass, this.ctxQueryHandler, resourceId);
	}

	private boolean delete(RoutingContext rtCtx, Class<?> javaClass, String resourceId) {
		return RouteUtils.delete(rtCtx, javaClass, this.ctxQueryHandler, resourceId);
	}
}
