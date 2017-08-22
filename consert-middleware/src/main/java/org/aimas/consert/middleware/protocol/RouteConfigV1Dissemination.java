package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxQueryHandler;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.ContextSubscription;
import org.aimas.consert.middleware.model.RDFObject;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandler;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;
import org.eclipse.rdf4j.query.resultio.QueryResultParser;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONParser;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;


/**
 * Defines the routes for a CtxQueryHandler agent in version 1
 */
public class RouteConfigV1Dissemination extends RouteConfigV1 {

	private final String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.ENGINE_ROUTE + "/answer_query/";
	
	private final String REQUEST_RESOURCE_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#RequestResource";
	
	private CtxQueryHandler ctxQueryHandler; // the agent that can be accessed with the defined routes
	
	private HttpClient client;  // the client to use for communications with other agents
	
	private AgentAddress engineConfig;
	
	private Repository convRepo;  // repository used for the conversion between Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // the connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversions 
	
	private Map<UUID, ServerWebSocket> sockets;  // sockets to use to notify the agents that their result is ready
	

	public RouteConfigV1Dissemination(CtxQueryHandler ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
		this.engineConfig = this.ctxQueryHandler.getEngineConfig();
		
		this.convRepo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);
		
		this.client = this.ctxQueryHandler.getVertx().createHttpClient();
		
		this.sockets = new HashMap<UUID, ServerWebSocket>();
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
		
		ServerWebSocket socket = rtCtx.request().upgrade();
		
		socket.textMessageHandler(new Handler<String>() {

			@Override
			public void handle(String str) {
				
				// Send the query to the engine
				client.get(engineConfig.getPort(), engineConfig.getIpAddress(), ANSWER_QUERY_ROUTE,
						new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						
						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								
								String result = buffer.toString();
								
								/*
								  if the status code is 201, then it is a long-lasting query, we store the socket
								  to send the result notification later, we create the resource and we send its
								  UUID to the agent that made the query
								*/
								if(resp.statusCode() == 201) {
									
									UUID resourceId = UUID.fromString(result);
									
									sockets.put(resourceId, socket);
									
									// Create the resource									
									AgentConfig ctxQHConfig = ctxQueryHandler.getAgentConfig();
									RequestResource resource = new RequestResource();
									resource.setResourceURI(URI.create("http://" + ctxQHConfig.getAddress() + ":"
											+ ctxQHConfig.getPort() + RouteConfig.API_ROUTE
											+ RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
											+ "/resources/" + resourceId.toString()));
									resource.setParticipantURI(URI.create("http://" + ctxQHConfig.getAddress() + ":"
											+ ctxQHConfig.getPort()));
									resource.setRequest(str);
									resource.setState(new RequestState(RequestState.REQ_RECEIVED));
									resource.setId(REQUEST_RESOURCE_URI + "/" + result);

									// Add the resource in CtxQueryHandler
									ctxQueryHandler.addResource(resourceId, resource);
								}
								
								// Send the results
								socket.writeTextMessage(result);
							}
						});
					}
				}).putHeader("content-type", "text/turtle").end(str);
			}
		});
	}

	/**
	 * POST subscribe for context
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxSubs(RoutingContext rtCtx) {

		String rdf = rtCtx.getBodyAsString();
		
		RepositoryConnection queryHandlerConn = this.ctxQueryHandler.getRepository().getConnection();
		
		// Convert the statements to a context subscription request Java object
		try {
			
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			this.convRepoConn.add(model);
			
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			
			RepositoryResult<Statement> assertionsStatements = this.convRepoConn.getStatements(null, rdfType, null);
			
			ContextSubscriptionRequest csr = null;
			while(assertionsStatements.hasNext()) {
				
				Statement s = assertionsStatements.next();
				
				if(s.getObject().stringValue().equals("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#"
						+ "ContextSubscriptionRequest")) {
					
					try {
						
						csr = (ContextSubscriptionRequest) this.convManager.get(s.getSubject(),
								ContextSubscriptionRequest.class);
						
					} catch(ClassCastException e) {
						continue;
					}
				}
			}
			
			this.convRepoConn.clear();
	
			// Insertion in CtxQueryHandler
			ContextSubscription cs = csr.getContextSubscription();
			RDFBeanManager queryHandlerManager = new RDFBeanManager(queryHandlerConn); 
			queryHandlerManager.add(cs);
			queryHandlerConn.close();

			// Create the resource
			UUID uuid = UUID.randomUUID();
			
			AgentConfig ctxQHConfig = this.ctxQueryHandler.getAgentConfig();
			RequestResource resource = new RequestResource();
			resource.setResourceURI(URI.create("http://" + ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort()
					+ RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE
					+ "/resources/" + uuid.toString()));
			resource.setInitiatorURI(csr.getInitiatorURI());
			resource.setParticipantURI(URI.create("http://" + ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort()));
			resource.setRequest(csr.getContextSubscription().getSubscriptionQuery());
			resource.setState(new RequestState(RequestState.REQ_RECEIVED));
			resource.setInitiatorCallbackURI(csr.getInitiatorCallbackURI());
			resource.setId(this.REQUEST_RESOURCE_URI + "/" + uuid.toString());

			// Add the resource in CtxQueryHandler
			this.ctxQueryHandler.addContextSubscription(uuid, cs, resource);
			
			// Answer by giving the UUID of the inserted object
			rtCtx.response().setStatusCode(201).putHeader("content-type", "text/plain").end(uuid.toString());

		} catch (RDFParseException | UnsupportedRDFormatException | IOException | RepositoryException | RDFBeanException e) {
			
			queryHandlerConn.close();
			System.err.println("Error while creating new context subscription: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}

	/**
	 * GET inspect context subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxSub(RoutingContext rtCtx) {

		// Get resource
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		ContextSubscription ctxSub = this.ctxQueryHandler.getContextSubscription(resourceUUID);

		// Send resource if found
		if (ctxSub != null) {
			this.get(rtCtx, ContextSubscription.class, ctxSub);
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
		String ctxSubsId = this.ctxQueryHandler.getContextSubscription(UUID.fromString(uuid)).getId();

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#ContextSubscription",
				ContextSubscription.class, ctxSubsId);

		if (entry != null) {

			ContextSubscription newCs = (ContextSubscription) entry.getValue();

			// Insertion in CtxQueryHandler
			this.ctxQueryHandler.setContextSubscription(UUID.fromString(uuid), newCs);
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
		String ctxSubsId = this.ctxQueryHandler.getContextSubscription(UUID.fromString(uuid)).getId();

		boolean done = this.delete(rtCtx, ContextSubscription.class, ctxSubsId);

		if (done) {

			// Remove old ContextSubscription from CtxQueryHandler
			ContextSubscription cs = this.ctxQueryHandler.removeContextSubscription(UUID.fromString(uuid));
			if (cs == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * GET resource
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetResource(RoutingContext rtCtx) {

		// Initialization
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		RequestResource resource = this.ctxQueryHandler.getResource(resourceUUID);

		// Send resource if found
		if (resource != null) {
			
			try {
				this.convManager.add(resource);
			} catch (RepositoryException | RDFBeanException e) {
				System.err.println("Error while convert resource " + resourceUUID.toString() + " to RDF statements: "
					+ e.getMessage());
				e.printStackTrace();
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			writer.startRDF();
			
			RepositoryResult<Statement> statements = this.convRepoConn.getStatements(null, null, null);
			
			while(statements.hasNext()) {
				writer.handleStatement(statements.next());
			}
			
			writer.endRDF();
			this.convRepoConn.clear();
			
			rtCtx.response().setStatusCode(200).end(baos.toString());
			
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * DELETE resource
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteResource(RoutingContext rtCtx) {

		// Initialization
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		RequestResource resource = this.ctxQueryHandler.removeResource(resourceUUID);

		// Send 200 code if the resource has been found
		if (resource != null) {
			rtCtx.response().setStatusCode(200).end();
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * PUT query result ready
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleResultReady(RoutingContext rtCtx) {

		// Initialization
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		RequestResource resource = this.ctxQueryHandler.getResource(resourceUUID);
		
		// Parse the received JSON and set the result
		List<BindingSet> results = new ArrayList<BindingSet>();
		
		QueryResultParser parser = new SPARQLResultsJSONParser();
		parser.setQueryResultHandler(new QueryResultHandler() {

			@Override
			public void handleBoolean(boolean value) throws QueryResultHandlerException {}

			@Override
			public void handleLinks(List<String> linkUrls) throws QueryResultHandlerException {}

			@Override
			public void startQueryResult(List<String> bindingNames)
					throws TupleQueryResultHandlerException {}

			@Override
			public void endQueryResult() throws TupleQueryResultHandlerException {}

			@Override
			public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
				results.add(bindingSet);
			}
			
		});
		InputStream is = new ByteArrayInputStream(rtCtx.getBody().getBytes());
		
		try {
			parser.parseQueryResult(is);
			
		} catch (QueryResultParseException | QueryResultHandlerException | IOException e) {
			
			System.err.println("Error while parsing JSON query result: " + e.getMessage());
			e.printStackTrace();
		}
		
		resource.setResult(results);
		
		// Send the UUID of the resource to the agent that made the query
		ServerWebSocket socket = this.sockets.get(resourceUUID);
		socket.writeTextMessage(resourceUUID.toString());
		
		rtCtx.response().setStatusCode(200).end();
	}
	
	/**
	 * PUT update subscriptions
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleUpdateSubs(RoutingContext rtCtx) {
		
		this.ctxQueryHandler.updateSubscriptions();
		
		rtCtx.response().setStatusCode(200).end();
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
