package org.aimas.consert.middleware.api;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

/**
 * This class provides an interface that allows to easily access different features from CONSERT middleware
 */
public class MiddlewareAPI {
	
	private final static String CONFIG_FILE = "agents.properties";  // path to the configuration file for the engine
	private final static String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.ENGINE_ROUTE + "/anwer_query/";  // route where the engine answers to queries

	/**
	 * Sends a query to the CONSERT engine in blocking mode
	 * @param query the query to be executed by the engine
	 * @return the result of the query
	 */
	public static String queryContext(String query) {

		String result = "";
		Future<Void> future = Future.future();
		
		// Send the query
		MiddlewareAPI.queryContext(query, new Handler<Buffer>() {

			@Override
			public void handle(Buffer buffer) {
		
				result.concat(buffer.toString());
				future.complete();
			}
		});
		
		// Wait for the result before returning it
		while(!future.isComplete()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * Sends a query to the CONSERT Engine in asynchronous mode
	 * @param query the query to be executed by the engine
	 * @param handler the handler to execute when the result of the query is received
	 */
	public static void queryContext(String query, Handler<Buffer> handler) {
		
		AgentConfig engineConfig = null;
		
		// Read the configuration to get the address of the engine
		try {
			
			Configuration config = new PropertiesConfiguration(MiddlewareAPI.CONFIG_FILE);
			engineConfig = AgentConfig.readConsertEngineConfig(config);
			
		} catch (ConfigurationException e) {
			
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		// Send the query
		HttpClient client = Vertx.vertx().createHttpClient();
		
		client.get(engineConfig.getPort(), engineConfig.getAddress(), MiddlewareAPI.ANSWER_QUERY_ROUTE,
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						resp.bodyHandler(handler);
					}
			
		}).end(query);
	}
}
