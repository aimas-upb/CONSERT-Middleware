package org.aimas.consert.middleware.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.protocol.ContextSubscriptionRequest;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;



/**
 * Interface that allows to easily access different features from CONSERT middleware
 */
public interface MiddlewareAPI {

	/**
	 * Sends a query to the CONSERT engine in blocking mode
	 * @param query the query to be executed by the engine
	 * @return the result of the query
	 */
	public String queryContext(String query);
	
	/**
	 * Sends a query to the CONSERT Engine in asynchronous mode
	 * @param query the query to be executed by the engine
	 * @param handler the handler to execute when the result of the query is received
	 */
	public void queryContext(String query, Handler<Buffer> handler);
	
	/**
	 * Subscribes to context updates in blocking mode
	 * @param request the context subscription request
	 * @return the UUID of the created context subscription
	 */
	public UUID subscribeContextUpdates(ContextSubscriptionRequest request);
	
	/**
	 * Gives a list of providers for a specific context assertion type
	 * @param ctxAssert the URI of the context assertion type
	 * @return a list of the agents that are able to provide the given context assertions
	 */
	public List<AgentSpec> listProviders(URI ctxAssert);
}
