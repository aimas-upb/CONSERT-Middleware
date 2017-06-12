package org.aimas.consert_middleware;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Server that asks Server2 to send it an URI, giving access to data
 */
public class Initiator extends AbstractVerticle {
	
	public static final String ADDRESS = "127.0.0.1"; // Address where other agents can reach this server
	public static final int LISTENING_PORT = 8080;    // Port where the server is listening
	public static final String HOST = "0.0.0.0";      // Host name that can be used to reach this server (0.0.0.0 = any)
	
	private static Vertx vertx = Vertx.vertx(); // Vertx instance
	
	private final int PARTICIPANT_PORT = 8081;                    // Port where the participant can be reached
	private final String PARTICIPANT_HOST = "127.0.0.1";          // Address where the participant can be reached
	private final String REQUEST_LONG_URI = "/user_long_query";   // URI to use on participant to make a long query
	private final String REQUEST_SHORT_URI = "/user_short_query"; // URI to use on participant to make a short query
	private final String CALLBACK_URI = "/query_result";          // URI on this server for the participant's callback
	

	public static void main(String[] args) {
		
		Initiator.vertx.deployVerticle(Initiator.class.getName());		
	}
	
	@Override
	public void start() {

		// Create routes
		Router router = Router.router(Initiator.vertx);
		
		router.route().handler(BodyHandler.create());
		
		router.get("/make_query/:type").handler(this::handleMakeQuery);
		router.post(this.CALLBACK_URI).handler(this::handleCallbackResult);

		// Start server
		Initiator.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(LISTENING_PORT, HOST, res -> {
				if (res.succeeded()) {
					System.out.println("Started server on port " + LISTENING_PORT + " host " + HOST);
				} else {
					System.out.println("Failed to start server on port " + LISTENING_PORT + " host " + HOST);
				}
			});
	}
	
	/**
	 * Handler for the "GET /make_query/:type" route
	 * @param rtCtx context for the handled request
	 */
	private void handleMakeQuery(RoutingContext rtCtx) {
		
		String query = "the query";
		String requestURI;
		
		// Determine if we make a long or a short query (for demo only)
		if(rtCtx.request().getParam("type").equals("short")) {
			requestURI = this.REQUEST_SHORT_URI;
		} else {
			requestURI = this.REQUEST_LONG_URI;
		}
		
		RequestBean reqBean = new RequestBean(Initiator.ADDRESS + ":" + Initiator.LISTENING_PORT,
			Initiator.ADDRESS + ":" + Initiator.LISTENING_PORT + this.CALLBACK_URI, query);
		
		// Receive the URI of the dynamically created route if the query is long, or the result otherwise
		HttpClient client = Initiator.vertx.createHttpClient();
		client.get(this.PARTICIPANT_PORT, this.PARTICIPANT_HOST, requestURI, new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {
				
				int statusCode = resp.statusCode();

				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						if(statusCode == 200) { // Long query, get resource URI to read result later
							System.out.println("Long query, waiting for result in resource at " + buffer.toString());
						} else if(statusCode == 201) { // Short query, get result now
							System.out.println("Short query, result = " + buffer.toString());
						} else {
							System.err.println("Unexpected status code " + statusCode);
						}
					}
				});
			}
		})
			.putHeader("content-type", "application/json")
			.end(Json.encodePrettily(reqBean));
		
		rtCtx.request().response().putHeader("content-type", "text/plain").setStatusCode(200).end("done");
	}
	
	/**
	 * Handler for route POST CALLBACK_URI
	 * @param rtCtx context for the handled request
	 */
	private void handleCallbackResult(RoutingContext rtCtx) {
		
		final String resourceURI = rtCtx.getBodyAsString();
		
		// Extract the information from the given URI
		String[] splittedURI = resourceURI.split(":");
		String host = splittedURI[0];
		int beginRoute = splittedURI[1].indexOf('/');
		int port = Integer.parseInt(splittedURI[1].substring(0, beginRoute));
		String route = splittedURI[1].substring(beginRoute);

		// Get result from resource
		HttpClient client = Initiator.vertx.createHttpClient();
		client.get(port, host, route, new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {

				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						RequestResource reqRes = Json.decodeValue(buffer.toString(), RequestResource.class);
						
						System.out.println("Got result for long query from " + resourceURI + ": " + reqRes.getResult());
						rtCtx.request().response().setStatusCode(200).end();
						
						// We don't need the resource anymore, so we delete it
						client.delete(port, host, route, new Handler<HttpClientResponse>(){

							@Override
							public void handle(HttpClientResponse event) {
							}}).end();
					}
				});
			}
		}).end();
	}
}
