package org.aimas.consert.middleware.protocol;

import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.model.RDFObject;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxQueryHandler agent in version 1
 */
public class RouteConfigV1Dissemination extends RouteConfigV1 {
	
	private CtxQueryHandler ctxQueryHandler;  // the agent that can be accessed with the defined routes
	
	
	public RouteConfigV1Dissemination(CtxQueryHandler ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
	}
	
	
	/**
	 * POST register query user
	 * @param rtCtx the routing context
	 */
	public void handlePostUnregQueryUser(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET query context
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxQuery(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * POST subscribe for context
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxSubs(RoutingContext rtCtx) {
		
		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#ContextSubscription",
				ContextSubscription.class);
		
		// Insertion in CtxQueryhandler
		ContextSubscription cs = (ContextSubscription) entry.getValue();
		this.ctxQueryHandler.addContextSubscription(entry.getKey(), cs);
	}
	
	/**
	 * GET inspect context subscription
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * PUT update context subscription
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxSub(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * DELETE unsubscribe for context
	 * @param rtCtx
	 */
	public void handleDeleteCtxSub(RoutingContext rtCtx) {
		// TODO
	}
	
	
	private Entry<UUID, Object> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass) {
		return RouteUtils.post(rtCtx, rdfClassName, javaClass, this.ctxQueryHandler);
	}
	
	private void get(RoutingContext rtCtx, Class<?> javaClass, RDFObject obj) {
		RouteUtils.get(rtCtx, javaClass, this.ctxQueryHandler, obj);
	}
	
	private Entry<UUID, Object> put(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass, String resourceId) {
		return RouteUtils.put(rtCtx, rdfClassName, javaClass, this.ctxQueryHandler, resourceId);
	}
	
	private boolean delete(RoutingContext rtCtx, Class<?> javaClass, String resourceId) {
		return RouteUtils.delete(rtCtx, javaClass, this.ctxQueryHandler, resourceId);
	}
}
