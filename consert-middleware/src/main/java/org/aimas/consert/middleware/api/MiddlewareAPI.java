package org.aimas.consert.middleware.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.protocol.ContextSubscriptionRequest;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
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
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

/**
 * This class provides an interface that allows to easily access different features from CONSERT middleware
 */
public abstract class MiddlewareAPI {
	
	private final static String CONFIG_FILE = "agents.properties";  // path to the configuration file for the engine
	
	private final static String ANSWER_QUERY_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.ENGINE_ROUTE + "/anwer_query/";  // route where the engine answers to queries
	private final static String CONTEXT_SUBSCRIPTION_ROUTE =  RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfigV1.DISSEMINATION_ROUTE + "/context_subscriptions/";  // route for context subscriptions
	private final static String ASSERTION_CAPABILITIES_ROUTE =  RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfigV1.COORDINATION_ROUTE + "/context_assertions/";  // route for context assertion capabilities
	
	private final static IRI RDF_TYPE = SimpleValueFactory.getInstance()
			.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	
	private final static IRI ASSERTION_CAPABILITY_IRI = SimpleValueFactory.getInstance()
			.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability");
	
	private static Repository convRepo;  // repository used to convert RDF statements and Java objects
	private static RepositoryConnection convRepoConn;  // connection to the conversion repository
	private static RDFBeanManager convManager;  // manager for the conversion repository
	private static HttpClient client;  // client to use for the communications with the agents
	
	private static AgentConfig engineConfig;  // configuration for communications with the CONSERT Engine
	private static AgentConfig ctxQueryHandlerConfig;  // configuration for communications with the CtxQueryHandler
	private static AgentConfig ctxCoordConfig;  // configuration for communications with the CtxCoord
	
	private static boolean isInitialized = false; // allows to know if the attribute variables have been initialized
	
	
	/**
	 * Initializes the attribute variables of this class if they haven't been yet
	 */
	private static void init() {
		
		if(!MiddlewareAPI.isInitialized) {
			
			// Read the configuration to get the required addresses
			try {
				
				Configuration config = new PropertiesConfiguration(MiddlewareAPI.CONFIG_FILE);
				MiddlewareAPI.engineConfig = AgentConfig.readConsertEngineConfig(config);
				MiddlewareAPI.ctxQueryHandlerConfig = AgentConfig.readCtxQueryHandlerConfig(config);
				MiddlewareAPI.ctxCoordConfig = AgentConfig.readCtxCoordConfig(config);
				
			} catch (ConfigurationException e) {
				
				System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
				e.printStackTrace();
			}

			MiddlewareAPI.convRepo = new SailRepository(new MemoryStore());
			MiddlewareAPI.convRepo.initialize();
			MiddlewareAPI.convRepoConn = MiddlewareAPI.convRepo.getConnection();
			MiddlewareAPI.convManager = new RDFBeanManager(MiddlewareAPI.convRepoConn);
			
			MiddlewareAPI.client = Vertx.vertx().createHttpClient();
		}
	}
	

	/**
	 * Sends a query to the CONSERT engine in blocking mode
	 * @param query the query to be executed by the engine
	 * @return the result of the query
	 */
	public static String queryContext(String query) {

		StringBuilder result = new StringBuilder();
		Future<Void> future = Future.future();
		
		MiddlewareAPI.init();
		
		// Send the query
		MiddlewareAPI.queryContext(query, new Handler<Buffer>() {

			@Override
			public void handle(Buffer buffer) {
		
				result.append(buffer.toString());
				future.complete();
			}
		});
		
		// Wait for the result before returning it
		while(!future.isComplete()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return result.toString();
	}
	
	/**
	 * Sends a query to the CONSERT Engine in asynchronous mode
	 * @param query the query to be executed by the engine
	 * @param handler the handler to execute when the result of the query is received
	 */
	public static void queryContext(String query, Handler<Buffer> handler) {
		
		MiddlewareAPI.init();
		
		// Send the query		
		MiddlewareAPI.client.get(engineConfig.getPort(), engineConfig.getAddress(), MiddlewareAPI.ANSWER_QUERY_ROUTE,
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						resp.bodyHandler(handler);
					}
			
		}).end(query);
	}
	
	
	/**
	 * Subscribes to context updates in blocking mode
	 * @param request the context subscription request
	 * @return the UUID of the created context subscription
	 */
	public static UUID subscribeContextUpdates(ContextSubscriptionRequest request) {
		
		MiddlewareAPI.init();
		StringBuilder uuid = new StringBuilder();
		Future<Void> future = Future.future();
		
		// Convert the ContextSubscriptionRequest Java object to RDF statements
		try {
			MiddlewareAPI.convManager.add(request);
		} catch (RepositoryException | RDFBeanException e) {
			System.err.println("Error while converting context subscription request to RDF statements: "
					+ e.getMessage());
			e.printStackTrace();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();
		
		RepositoryResult<Statement> statements = MiddlewareAPI.convRepoConn.getStatements(null, null, null);
		
		while(statements.hasNext()) {
			writer.handleStatement(statements.next());
		}
		
		writer.endRDF();
		MiddlewareAPI.convRepoConn.clear();
		
		// Send the context subscription
		MiddlewareAPI.client.post(MiddlewareAPI.ctxQueryHandlerConfig.getPort(),
				MiddlewareAPI.ctxQueryHandlerConfig.getAddress(),MiddlewareAPI.CONTEXT_SUBSCRIPTION_ROUTE,
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						
						resp.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer) {
								
								if(resp.statusCode() == 201) {
									uuid.append(buffer.toString());
								}
								
								future.complete();
							}
						});
					}
			
		}).putHeader("content-type", "text/turtle").end(baos.toString());
		
		// Wait for the result before returning it
		while(!future.isComplete()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return UUID.fromString(uuid.toString());
	}
	
	public static List<AgentSpec> listProviders(URI ctxAssert, List<String> providersIdentifiers) {
		
		MiddlewareAPI.init();
		
		List<AgentSpec> providers = new ArrayList<AgentSpec>();
		
		// Get all the assertion capabilities from each provider
		for(String id : providersIdentifiers) {
			
			Future<Void> future = Future.future();
			
			MiddlewareAPI.client.get(MiddlewareAPI.ctxCoordConfig.getPort(), MiddlewareAPI.ctxCoordConfig.getAddress(),
					MiddlewareAPI.ASSERTION_CAPABILITIES_ROUTE + "?agentIdentifier=" + id,
					new Handler<HttpClientResponse>() {
	
				@Override
				public void handle(HttpClientResponse resp) {
	
					resp.bodyHandler(new Handler<Buffer>() {
	
						@Override
						public void handle(Buffer buffer) {
	
							try {
								
								// Convert the received RDF statements to Java objects
								Model model = Rio.parse(new ByteArrayInputStream(buffer.getBytes()), "",
										RDFFormat.TURTLE);
								MiddlewareAPI.convRepoConn.add(model);
								
								RepositoryResult<Statement> statements = MiddlewareAPI.convRepoConn
										.getStatements(null, MiddlewareAPI.RDF_TYPE,
												MiddlewareAPI.ASSERTION_CAPABILITY_IRI);
								
								while(statements.hasNext()) {
									
									Statement s = statements.next();
										
									AssertionCapability ac = MiddlewareAPI.convManager.get(s.getSubject(),
											AssertionCapability.class);
									
									// If the assertion capability is for the requested context assertion,
									// store its provider to return it at the end of the method
									if(ac.getContent().equals(ctxAssert)) {
										
										providers.add(ac.getProvider());
									}
								}
								
							} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
								System.err.println("Error while converting RDF statements to Java objects: "
										+ e.getMessage());
								e.printStackTrace();
							}
							
							future.complete();
						}
					});
				}
			}).end();
			
			// Wait for the result before continuing
			while(!future.isComplete()) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return providers;
	}
}
