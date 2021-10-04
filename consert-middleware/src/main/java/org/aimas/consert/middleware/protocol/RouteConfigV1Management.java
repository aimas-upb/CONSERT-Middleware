package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayOutputStream;

import org.aimas.consert.middleware.agents.OrgMgr;
import org.aimas.consert.middleware.config.Request;
import org.aimas.consert.middleware.model.AgentAddress;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for an OrgMgr agent in version 1
 */
public class RouteConfigV1Management extends RouteConfigV1 {

	private OrgMgr orgMgr; // the agent that can be accessed with the defined routes
	
	private Repository convRepo;  // repository used to convert Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // connection to the conversion repository
	private RDFBeanManager convManager;  // manager for the conversion repository
	
	private int port;  // value of the next port to attribute

	public RouteConfigV1Management(OrgMgr orgMgr) {
		this.orgMgr = orgMgr;
		
		this.convRepo = new SailRepository(new MemoryStore());
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
		this.convManager = new RDFBeanManager(this.convRepoConn);
		
		this.port = 8081;
	}

	/**
	 * POST register agent
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxAgents(RoutingContext rtCtx) {
		
		String agent = rtCtx.getBodyAsString();
		String type = rtCtx.request().method().name();
		String remoteHost = rtCtx.request().remoteAddress().host();
		int remotePort = rtCtx.request().remoteAddress().port();
		Request.logInfo(remoteHost, remotePort, type, agent);

		// Set the values for the configuration of the new agent
		AgentAddress addr = new AgentAddress();
		addr.setIpAddress(rtCtx.request().remoteAddress().host());
		addr.setPort(this.port);
		
		this.port++;
		
		// Add the new agent in the OrgMgr's knowledge in function of its type
		switch(agent) {
			
			case "CtxCoord":
				this.orgMgr.setCtxCoord(addr);
				break;
				
			case "CtxSensor":
				this.orgMgr.addCtxSensor(addr);
				break;
				
			case "CtxUser":
				this.orgMgr.setCtxUser(addr);
				break;
				
			case "CtxQueryHandler":
				this.orgMgr.setCtxQueryHandler(addr);
				break;
				
			default:
				rtCtx.response().setStatusCode(404).end();
				return;
		}
		
		// Convert the new configuration to RDF statements and send them in the response
		try {
			
			this.convManager.add(addr);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			writer.startRDF();
			
			RepositoryResult<Statement> statements = this.convRepoConn.getStatements(null, null, null);
			
			while(statements.hasNext()) {
				writer.handleStatement(statements.next());
			}
			
			writer.endRDF();
			
			this.convRepoConn.clear();
			rtCtx.response().setStatusCode(200).putHeader("content-type", "text/turtle").end(baos.toString());
			
		} catch (RepositoryException | RDFBeanException e) {

			System.err.println("Error while getting information for CtxCoord address and port: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}

	/**
	 * GET find coordinator
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetFindCoord(RoutingContext rtCtx) {
		String type = rtCtx.request().method().name();
		String remoteHost = rtCtx.request().remoteAddress().host();
		int remotePort = rtCtx.request().remoteAddress().port();
		Request.logInfo(remoteHost, remotePort, type, "asd");
		findAgent(rtCtx, this.orgMgr.getCtxCoord());
	}

	/**
	 * GET find query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetFindQueryHandler(RoutingContext rtCtx) {
		String type = rtCtx.request().method().name();
		String remoteHost = rtCtx.request().remoteAddress().host();
		int remotePort = rtCtx.request().remoteAddress().port();
		Request.logInfo(remoteHost, remotePort, type, null);
		findAgent(rtCtx, this.orgMgr.getCtxQueryHandler());
	}
	
	
	/**
	 * Generic method to give the configuration of a specified agent
	 * @param rtCtx the routing context to use to send the response
	 * @param agent the configuration to send
	 */
	private void findAgent(RoutingContext rtCtx, AgentAddress agent) {
		
		// send 404 code if the agent is unknown
		if(agent == null) {
			rtCtx.response().setStatusCode(404).end();
			return;
		}
		
		// convert the configuration to RDF statements and send them in the reponse
		try {
			
			this.convManager.add(agent);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			writer.startRDF();
			
			RepositoryResult<Statement> statements = this.convRepoConn.getStatements(null, null, null);
			
			while(statements.hasNext()) {
				writer.handleStatement(statements.next());
			}
			
			writer.endRDF();
			
			this.convRepoConn.clear();
			rtCtx.response().setStatusCode(200).putHeader("content-type", "text/turtle").end(baos.toString());
			
		} catch (RepositoryException | RDFBeanException e) {

			System.err.println("Error while getting information for agent address and port: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}
}
