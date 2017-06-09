package org.aimas.rest_test;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server2 extends AbstractVerticle {
	
	public static final String ADRESS = "127.0.0.1";
	public static final int LISTENING_PORT = 8081;
	public static final String HOST = "0.0.0.0";
	
	private static Vertx vertx = Vertx.vertx();
	private static Router router = Router.router(Server2.vertx);

	
	public static void main(String[] args) {
		
		Server2.vertx.deployVerticle(Server2.class.getName());		
	}
	
	@Override
	public void start() {
		
		Server2.router.route().handler(BodyHandler.create());
		
		Server2.router.get("/ask-server1").handler(this::handleAskServer1);
		
		Server2.vertx.createHttpServer()
			.requestHandler(Server2.router::accept)
			.listen(LISTENING_PORT, HOST, res -> {
				if (res.succeeded()) {
					System.out.println("Started server on port " + LISTENING_PORT + " host " + HOST);
				} else {
					System.out.println("Failed to start server on port " + LISTENING_PORT + " host " + HOST);
				}
			});
	}
	
	// Handler for the "/ask-server1" route
	private void handleAskServer1(RoutingContext rtCtx) {
		
		// Dynamically create route
		String dynRouteName = "/" + UUID.randomUUID().toString();

		Server2.router.get(dynRouteName).handler(respRtCtx -> {
			respRtCtx.response()
				.putHeader("content-type", "text/plain")
				.setStatusCode(200)
				.end("resource");
		});
		
		// Send the URI of the dynamically created route
		rtCtx.response()
			.putHeader("content-type", "text/plain")
			.setStatusCode(200)
			.end(Server2.ADRESS + ":" + Server2.LISTENING_PORT + dynRouteName);
		
		
		/*
		// Get message from Server1
		HttpClient client = Server2.vertx.createHttpClient();
		client.get(this.SERVER1_PORT, this.SERVER1_HOST, this.REQUEST_URI, new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						
						// Return received message
						HttpServerResponse response = rtCtx.response();
						response.putHeader("content-type", "text/plain")
							.setStatusCode(200)  // 200 by default
							.end("Server1 replied: " + buffer.getString(0, buffer.length()));
					}
				});
			}
		}).end();
		*/
	}
}
