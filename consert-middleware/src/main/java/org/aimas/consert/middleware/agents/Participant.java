package org.aimas.consert.middleware.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.aimas.consert.middleware.protocol.RequestBean;
import org.aimas.consert.middleware.protocol.RequestResource;
import org.aimas.consert.middleware.protocol.RequestState;

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
 * Server that gives access to data through a dynamically created URI
 */
public class Participant extends AbstractVerticle {
	
	public static final String ADDRESS = "127.0.0.1"; // Address where other agents can reach this server
	public static final int LISTENING_PORT = 8081;    // Port where the server is listening
	public static final String HOST = "0.0.0.0";      // Host name that can be used to reach this server (0.0.0.0 = any)
	
	private static Vertx vertx = Vertx.vertx(); // Vertx instance

	private Map<UUID, RequestResource> resources; // List of reachable resources, identified by their UUID
	
	
	public static void main(String[] args) {
		
		Participant.vertx.deployVerticle(Participant.class.getName());		
	}
	
	@Override
	public void start() {
		
		this.resources = new HashMap<UUID, RequestResource>();
		
		// Create routes
		Router router = Router.router(Participant.vertx);
		
		router.route().handler(BodyHandler.create());
		
		router.get("/user_long_query").handler(this::handleUserLongQuery);
		router.get("/user_short_query").handler(this::handleUserShortQuery);
		
		router.post("/resources").handler(this::handleCreateResource);
		router.get("/resources").handler(this::handleGetAllResources);
		router.get("/resources/:uuid").handler(this::handleGetResource);
		router.put("/resources/:uuid").handler(this::handleUpdateResource);
		router.delete("/resources/:uuid").handler(this::handleDeleteResource);
		
		// Start server
		Participant.vertx.createHttpServer()
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
	 * Handler for the "GET /user_long_query" route
	 * @param rtCtx context for the handled request
	 */
	private void handleUserLongQuery(RoutingContext rtCtx) {
		
		Handler<HttpClientResponse> ignoreResponseHandler = new Handler<HttpClientResponse>() {
			@Override
			public void handle(HttpClientResponse event) {
			}
		};
		
		// Dynamically create route through "POST /resource" route
		RequestBean reqBean = Json.decodeValue(rtCtx.getBodyAsString(), RequestBean.class);
		
		String initiatorURI = reqBean.getInitiatorURI();
		String initiatorCallbackURI = reqBean.getInitiatorCallbackURI();

		HttpClient client = Participant.vertx.createHttpClient();
		client.post(Participant.LISTENING_PORT, "127.0.0.1", "/resources", new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						// Get the UUID of the created resource
						String resourceUUID = buffer.toString();
						String routeUUID = "/resources/" + resourceUUID;
						
						// Send the URI of the dynamically created route (agree)
						rtCtx.response()
							.putHeader("content-type", "text/plain")
							.setStatusCode(200)
							.end(Participant.ADDRESS + ":" + Participant.LISTENING_PORT + routeUUID);
						
						client.put(Participant.LISTENING_PORT, "127.0.0.1", routeUUID, ignoreResponseHandler)
							.putHeader("content-type", "text/plain")
							.end("state=agree_sent");
						
						// Get result
						String result = "the result";
						
						client.put(Participant.LISTENING_PORT, "127.0.0.1", routeUUID, ignoreResponseHandler)
							.putHeader("content-type", "text/plain")
							.end("result=" + result);
						

						// Extract the information from the initiator callback URI 
						String[] splittedURI = initiatorURI.split(":");
						String initiatorHost = splittedURI[0];
						int initiatorPort = Integer.parseInt(splittedURI[1]);
						String callbackRoute = initiatorCallbackURI.substring(initiatorCallbackURI.indexOf('/'));
						
						// Send notification to initiator
						client.put(Participant.LISTENING_PORT, "127.0.0.1", routeUUID, ignoreResponseHandler)
							.putHeader("content-type", "text/plain")
							.end("state=result_sent");
						
						client.post(initiatorPort, initiatorHost, callbackRoute, ignoreResponseHandler)
							.putHeader("content-type", "text/plain")
							.end(Participant.ADDRESS + ":" + Participant.LISTENING_PORT + routeUUID);					
					}
				});
			}
		})
			.putHeader("content-type", "application/json")
			.end(rtCtx.getBodyAsString());
	}
	
	/**
	 * Handler for the "GET /user_short_query" route
	 * @param rtCtx context for the handled request
	 */
	private void handleUserShortQuery(RoutingContext rtCtx) {
		
		String result = "the result";

		// Directly send the result
		rtCtx.response()
			.putHeader("content-type", "text/plain")
			.setStatusCode(201)
			.end(result);
	}
	
	
	/**
	 * Handler for the "POST /resources" route
	 * @param rtCtx context for the handled request
	 */
	private void handleCreateResource(RoutingContext rtCtx) {

		// Create UUID for the new resource
		UUID resourceUUID = UUID.randomUUID();
		System.out.println("create resource " + resourceUUID.toString());
		
		RequestBean reqBean = Json.decodeValue(rtCtx.getBodyAsString(), RequestBean.class);
		
		// Create the resource
		RequestResource reqRes = new RequestResource();
		reqRes.setResourceURI(Participant.ADDRESS + ":" + Participant.LISTENING_PORT + "/" + resourceUUID.toString());
		reqRes.setInitiatorURI(reqBean.getInitiatorURI());
		reqRes.setParticipantURI(Participant.ADDRESS + ":" + Participant.LISTENING_PORT);
		reqRes.setRequest(reqBean.getRequest());
		reqRes.setState(RequestState.REQ_RECEIVED);
		reqRes.setInitiatorCallbackURI(reqBean.getInitiatorCallbackURI());
		
		this.resources.put(resourceUUID, reqRes);
		
		// Reply with generated UUID for the new resource
		rtCtx.response().setStatusCode(201).putHeader("content-type", "text/plain").end(resourceUUID.toString());
	}
	
	/**
	 * Handler for the "GET /resources" route
	 * @param rtCtx context for the handled request
	 */
	private void handleGetAllResources(RoutingContext rtCtx) {
		
		// Send resources
		rtCtx.response()
			.putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(200)
			.end(Json.encodePrettily(this.resources));
	}
	
	/**
	 * Handler for the "GET /resources/:uuid" route
	 * @param rtCtx context for the handled request
	 */
	private void handleGetResource(RoutingContext rtCtx) {

		// Get resource
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("uuid"));
		RequestResource resource = this.resources.get(resourceUUID);
		
		// Send resource if found
		if(resource != null) {
			rtCtx.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200)
				.end(Json.encodePrettily(resource));
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * Handler for the "PUT /resources/:uuid" route
	 * @param rtCtx context for the handled request
	 */
	private void handleUpdateResource(RoutingContext rtCtx) {

		// Get resource
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("uuid"));
		RequestResource resource = this.resources.get(resourceUUID);
		
		// Update resource if found
		if(resource != null) {
			
			boolean updateOk = true;
			String body = rtCtx.getBodyAsString();
			
			if(body.equals("state=agree_sent")) {
				resource.setState(RequestState.AGREE_SENT);
				System.out.println("update resource " + resourceUUID + ": state = agree_sent");
			} else if(body.equals("state=result_sent")) {
				resource.setState(RequestState.RESULT_SENT);
				System.out.println("update resource " + resourceUUID + ": state = result_sent");
			} else if(body.startsWith("result=")) {
				resource.setResult(body.substring("result=".length()));
				System.out.println("update resource " + resourceUUID + ": result = "
					+ body.substring("result=".length()));
			} else {
				updateOk = false;
				rtCtx.response().setStatusCode(204).end();
				System.out.println("update resource " + resourceUUID + ": error");
			}
			
			if(updateOk) {
				rtCtx.response().setStatusCode(200).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * Handler for the "DELETE /resources/:uuid" route
	 * @param rtCtx context for the handled request
	 */
	private void handleDeleteResource(RoutingContext rtCtx) {
		
		// Remove resource
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("uuid"));
		RequestResource resource = this.resources.remove(resourceUUID);
		
		System.out.println("delete resource " + resourceUUID);
		
		if(resource != null) {
			rtCtx.response().setStatusCode(200).end();
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
}
