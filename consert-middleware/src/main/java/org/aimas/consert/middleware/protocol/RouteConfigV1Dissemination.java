package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
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
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.RoutingContext;


/**
 * Defines the routes for a CtxQueryHandler agent in version 1
 */
public class RouteConfigV1Dissemination extends RouteConfigV1 {

	private final String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.ENGINE_ROUTE + "/answer_query/";
	
	private CtxQueryHandler ctxQueryHandler; // the agent that can be accessed with the defined routes
	
	private HttpClient client;  // the client to use for communications with other agents
	
	private AgentAddress engineConfig;
	
	private Repository convRepo;  // repository used for the conversion between Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // the connection to the conversion repository

	public RouteConfigV1Dissemination(CtxQueryHandler ctxQueryHandler) {
		this.ctxQueryHandler = ctxQueryHandler;
		this.engineConfig = this.ctxQueryHandler.getEngineConfig();
		
		this.convRepo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		
		this.client = this.ctxQueryHandler.getVertx().createHttpClient();
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
		
		System.out.println("CtxQueryHandler send query to engine: " + this.engineConfig);
		// Send the query to the engine
		this.client.get(this.engineConfig.getPort(), this.engineConfig.getIpAddress(), this.ANSWER_QUERY_ROUTE,
				new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						
						// Short-lasting queries
						
						// Send the results
						rtCtx.response().setStatusCode(resp.statusCode()).putHeader("content-type", "text/plain")
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

		String rdf = rtCtx.getBodyAsString();
		
		RepositoryConnection queryHandlerConn = this.ctxQueryHandler.getRepository().getConnection();
		
		// Convert the statements to a context subscription request Java object
		try {
			
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			this.convRepoConn.add(model);
			RDFBeanManager manager = new RDFBeanManager(this.convRepoConn);
			
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			
			RepositoryResult<Statement> assertionsStatements = this.convRepoConn.getStatements(null, rdfType, null);
			
			ContextSubscriptionRequest csr = null;
			while(assertionsStatements.hasNext()) {
				
				Statement s = assertionsStatements.next();
				
				if(s.getObject().stringValue().equals("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#"
						+ "ContextSubscriptionRequest")) {
					
					try {
						
						csr = (ContextSubscriptionRequest) manager.get(s.getSubject(), ContextSubscriptionRequest.class);
						
					} catch(ClassCastException e) {
						continue;
					}
				}
			}
	
			// Insertion in CtxQueryHandler
			ContextSubscription cs = csr.getContextSubscription();
			RDFBeanManager queryHandlerManager = new RDFBeanManager(queryHandlerConn); 
			queryHandlerManager.add(cs);
			queryHandlerConn.close();

			// Create the resource
			UUID uuid = UUID.randomUUID();
			
			AgentConfig ctxQHConfig = this.ctxQueryHandler.getAgentConfig();
			RequestResource resource = new RequestResource();
			resource.setResourceURI(URI.create("http://" + ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort() + RouteConfig.API_ROUTE
					+ RouteConfigV1.VERSION_ROUTE + RouteConfig.DISSEMINATION_ROUTE + "/resources/"
					+ uuid.toString()));
			resource.setInitiatorURI(csr.getInitiatorURI());
			resource.setParticipantURI(URI.create("http://" + ctxQHConfig.getAddress() + ":" + ctxQHConfig.getPort()));
			resource.setRequest(csr.getContextSubscription().getSubscriptionQuery());
			resource.setState(new RequestState(RequestState.REQ_RECEIVED));
			resource.setInitiatorCallbackURI(csr.getInitiatorCallbackURI());

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
	public void handleResources(RoutingContext rtCtx) {

		// Initialization
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		RequestResource resource = this.ctxQueryHandler.getResource(resourceUUID);

		// Send resource if found
		if (resource != null) {
			this.get(rtCtx, RequestResource.class, resource);
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
	
	/**
	 * POST query result ready
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleResultReady(RoutingContext rtCtx) {
		
		// Initialization
		UUID resourceUUID = UUID.fromString(rtCtx.request().getParam("id"));
		RequestResource resource = this.ctxQueryHandler.getResource(resourceUUID);
		
		System.out.println("received result notification for resource " + resource);
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
