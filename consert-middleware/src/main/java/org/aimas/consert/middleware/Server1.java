package org.aimas.consert.middleware;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Server that asks Server2 to send it an URI, giving access to data
 */
public class Server1 extends AbstractVerticle {
	
	public static final int LISTENING_PORT = 8080;
	public static final String HOST = "0.0.0.0";
	
	private static Vertx vertx = Vertx.vertx();
	
	private final int SERVER2_PORT = 8081;
	private final String SERVER2_HOST = "127.0.0.1";
	private final String REQUEST_URI = "/whiskies";

	public static void main(String[] args) {
		
		Server1.vertx.deployVerticle(Server1.class.getName());		
	}
	
	@Override
	public void start() {
		
		Router router = Router.router(Server1.vertx);
		
		router.get("/").handler(this::handle);
		router.get("/:id").handler(this::handleGetOne);
		
		Server1.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(LISTENING_PORT, HOST, res -> {
				if (res.succeeded()) {
					System.out.println("Started server on port " + LISTENING_PORT + " host " + HOST);
				} else {
					System.out.println("Failed to start server on port " + LISTENING_PORT + " host " + HOST);
				}
			});
	}
	
	// Handler for the "/" route
	private void handle(RoutingContext rtCtx) {
		
		// Receive the URI of the dynamically created route
		HttpClient client = Server1.vertx.createHttpClient();
		client.get(this.SERVER2_PORT, this.SERVER2_HOST, this.REQUEST_URI, new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						getDynRoute(rtCtx, buffer);
					}
				});
			}
		}).end();
	}
	
	// Handler for the "/:id" route
	private void handleGetOne(RoutingContext rtCtx) {
		
		// Receive the URI of the dynamically created route
		HttpClient client = Server1.vertx.createHttpClient();
		client.get(this.SERVER2_PORT, this.SERVER2_HOST, this.REQUEST_URI + "/" + rtCtx.request().getParam("id"),
				new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						getDynRoute(rtCtx, buffer);
					}
				});
			}
		}).end();
	}
	
	
	// Sends GET to the dynamically created route and returns the result
	private void getDynRoute(RoutingContext rtCtx, Buffer buffer) {

		String dynRouteURI = buffer.getString(0, buffer.length());
		
		// Extract the information from the given URI 
		String[] splittedURI = dynRouteURI.split(":");
		String host = splittedURI[0];
		int beginRoute = splittedURI[1].indexOf('/');
		int port = Integer.parseInt(splittedURI[1].substring(0, beginRoute));
		String route = splittedURI[1].substring(beginRoute);
		
		// Get data from the given URI
		HttpClient client = Server1.vertx.createHttpClient();
		client.get(port, host, route, new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						// Return received message						
						HttpServerResponse response = rtCtx.response();
						response.putHeader("content-type", "text/plain; charset=utf-8")
							.setStatusCode(200)  // 200 by default
							.end("Got from URI " + dynRouteURI + ":\n" + buffer.toString() + "\n");
					}
				});
			}
		}).end();
	}
}
