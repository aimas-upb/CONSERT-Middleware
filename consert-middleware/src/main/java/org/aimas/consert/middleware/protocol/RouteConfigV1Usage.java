package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxUser;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.middleware.model.tasking.AlterUpdateModeCommand;
import org.aimas.consert.middleware.model.tasking.StartUpdatesCommand;
import org.aimas.consert.middleware.model.tasking.StopUpdatesCommand;
import org.aimas.consert.middleware.model.tasking.TaskingCommand;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxUser agent in version 1
 */
public class RouteConfigV1Usage extends RouteConfigV1 {
	
	private static final String START_UPDATES_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StartUpdatesCommand";
	private static final String STOP_UPDATES_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StopUpdatesCommand";
	private static final String ALTER_UPDATE_MODE_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AlterUpdateModeCommand";
	
	private static final String SUBSCRIPTIONS_RESOURCES_ROUTE = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE
			+ RouteConfig.DISSEMINATION_ROUTE + "/resources/";

	private CtxUser ctxUser; // the agent that can be accessed with the defined routes
	private AssertionUpdateMode defaultUpdateMode; // the default update mode used when starting updates
	
	private HttpClient client;  // client to use for the communications with other agents

	public RouteConfigV1Usage(CtxUser ctxUser) {
		this.ctxUser = ctxUser;
		this.defaultUpdateMode = new AssertionUpdateMode();
		this.defaultUpdateMode.setUpdateMode(AssertionUpdateMode.TIME_BASED);
		this.defaultUpdateMode.setUpdateRate(500);
		//this.defaultUpdateMode.setUpdateMode(AssertionUpdateMode.CHANGE_BASED);
		//this.defaultUpdateMode.setUpdateRate(0);
		
		this.client = this.ctxUser.getVertx().createHttpClient();
	}

	/**
	 * PUT tasking command
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		
		String rdf = rtCtx.getBodyAsString();

		RepositoryConnection conn = this.ctxUser.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);
		
		TaskingCommand taskingCommand = null;

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			// Getting the object we just inserted
			for (Statement s : model) {
				
				String rdfClass = s.getObject().stringValue();
				
				if (rdfClass.equals(START_UPDATES_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), StartUpdatesCommand.class);
					break;
				} else if (rdfClass.equals(STOP_UPDATES_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), StopUpdatesCommand.class);
					break;
				} else if (rdfClass.equals(ALTER_UPDATE_MODE_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), AlterUpdateModeCommand.class);
					break;
				}
			}

			conn.close();
			
			AgentAddress address = taskingCommand.getTargetAgent().getAddress();
			AgentConfig agentConfig = this.ctxUser.getAgentConfig();
			if(address.getIpAddress().equals(agentConfig.getAddress()) && address.getPort() == agentConfig.getPort()) {
				
				if(taskingCommand instanceof StartUpdatesCommand) {
					this.ctxUser.startUpdates(((StartUpdatesCommand) taskingCommand).getTargetAssertion(),
							this.defaultUpdateMode);
				} else if(taskingCommand instanceof StopUpdatesCommand) {
					this.ctxUser.stopUpdates(((StopUpdatesCommand) taskingCommand).getTargetAssertion());
				} else if(taskingCommand instanceof AlterUpdateModeCommand) {
					this.ctxUser.alterUpdates(((AlterUpdateModeCommand) taskingCommand).getTargetAssertion(),
							((AlterUpdateModeCommand) taskingCommand).getUpdateMode());
				} else {
					rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
					return;
				}
			} else {
				rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
				return;
			}

			// Answer with code 200
			rtCtx.response().setStatusCode(200).end();
		} catch (RDF4JException | RDFBeanException | IOException e) {

			conn.close();
			System.err.println("Error while getting tasking command: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
		}
	}
	
	/**
	 * POST query result ready
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleResultReady(RoutingContext rtCtx) {
		
		// Initialization
		String resourceUUID = rtCtx.request().getParam("id");
		
		AgentAddress queryHandler = this.ctxUser.getCtxQueryHandlerConfig();
		this.client.get(queryHandler.getPort(), queryHandler.getIpAddress(),
				RouteConfigV1Usage.SUBSCRIPTIONS_RESOURCES_ROUTE + resourceUUID, new Handler<HttpClientResponse>() {

			@Override
			public void handle(HttpClientResponse resp) {
				
				resp.bodyHandler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer buffer) {
						System.out.println("received result notification for resource " + buffer.toString());
					}
				});
			}
		}).end();
	}
}
