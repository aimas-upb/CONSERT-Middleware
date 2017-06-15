package org.aimas.consert.middleware;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Server that gives access to data through a dynamically created URI
 */
public class Server2 extends AbstractVerticle {
	
	public static final String ADRESS = "127.0.0.1";
	public static final int LISTENING_PORT = 8081;
	public static final String HOST = "0.0.0.0";
	
	private static Vertx vertx = Vertx.vertx();
	private static Router router = Router.router(Server2.vertx);
	
	private Map<Integer, Whisky> products;

	
	public static void main(String[] args) {
		
		Server2.vertx.deployVerticle(Server2.class.getName());		
	}
	
	@Override
	public void start() {
		
		createData();
		
		Server2.router.route().handler(BodyHandler.create());
		
		Server2.router.get("/whiskies").handler(this::handleGetAll);
		Server2.router.get("/whiskies/:id").handler(this::handleGetOne);
		
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
	
	// Handler for the "/whiskies" route
	private void handleGetAll(RoutingContext rtCtx) {
		
		// Dynamically create route
		String dynRouteName = "/" + UUID.randomUUID().toString();

		Server2.router.get(dynRouteName).handler(respRtCtx -> {
			respRtCtx.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200)
				.end(Json.encodePrettily(this.products.values()));
		});
		
		// Send the URI of the dynamically created route
		rtCtx.response()
			.putHeader("content-type", "text/plain")
			.setStatusCode(200)
			.end(Server2.ADRESS + ":" + Server2.LISTENING_PORT + dynRouteName);
	}
	
	// Handler for the "/whiskies/:id" route
	private void handleGetOne(RoutingContext rtCtx) {
		
		// Dynamically create route
		String dynRouteName = "/" + UUID.randomUUID().toString();

		Server2.router.get(dynRouteName).handler(respRtCtx -> {
			
			String id = rtCtx.request().getParam("id");
			
			if (id == null) {
				respRtCtx.response().setStatusCode(400).end();
			} else {
				Whisky whisky = this.products.get(Integer.parseInt(id));
				
				if (whisky == null) {
					respRtCtx.response().setStatusCode(404).end();
				} else {
					respRtCtx.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(200)
						.end(Json.encodePrettily(whisky));
				}
			}
		});
		
		// Send the URI of the dynamically created route
		rtCtx.response()
			.putHeader("content-type", "text/plain")
			.setStatusCode(200)
			.end(Server2.ADRESS + ":" + Server2.LISTENING_PORT + dynRouteName);
	}
	
	private void createData() {
		
		this.products = new HashMap<Integer, Whisky>();
		
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		this.products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
	  	this.products.put(talisker.getId(), talisker);
	}
}
