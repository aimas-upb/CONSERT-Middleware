package org.aimas.consert.middleware.agents;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.StartUpdatesCommand;
import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

public class CtxCoordImplTest extends CtxCoord {
	
	private ScheduledExecutorService taskingCommandsService;  // allows to start the updates thanks to tasking commands 

	@Override
	public void start(Future<Void> future) {
		
		super.start(future);
		
		this.taskingCommandsService = Executors.newScheduledThreadPool(1);
		
		// wait before sending the tasking commands so that the CtxSensors can start 
		this.taskingCommandsService.schedule(new SendStartTaskingCommands(), 2000, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void stop() {
		
		super.stop();
		this.taskingCommandsService.shutdownNow();
	}
	
	
	private class SendStartTaskingCommands implements Runnable {
		
		public void run() {
			
			// Send "start updates" tasking commands
			
			HttpClient client = vertx.createHttpClient();
			String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.SENSING_ROUTE
					+ "/tasking_command/";
			
			// First, we need to convert the objects to RDF statements
			// We use the repository for this
			Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			RepositoryConnection conn = repo.getConnection();
			RDFBeanManager manager = new RDFBeanManager(conn);
			
			// Prepare to write the RDF statements
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			RepositoryResult<Statement> iter = null;
			
			for(AssertionCapability ac : assertionCapabilities.values()) {
				
				StartUpdatesCommand taskingCommand = new StartUpdatesCommand();
				taskingCommand.setTargetAgent(ac.getProvider());
				taskingCommand.setTargetAssertion(ac.getContent());

				writer.startRDF();
				
				try {
					manager.add(taskingCommand);
				} catch (RepositoryException | RDFBeanException e) {
					e.printStackTrace();
				}
				
				// Write the statements
				iter = conn.getStatements(null, null, null);
				while(iter.hasNext()) {
					writer.handleStatement(iter.next());
				}
				
				writer.endRDF();
				
				AgentAddress address = ac.getProvider().getAddress();
				
				client.put(address.getPort(), address.getIpAddress(), route, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						if(resp.statusCode() != 200) {
							System.err.println("CtxSensor at " + address.getIpAddress() + ":" + address.getPort()
								+ " could not start updates: " + resp.statusCode() + " " + resp.statusMessage());
						}
					}
				}).end(baos.toString());
				
				baos.reset();
				conn.clear();
			}
			
			conn.close();
			repo.shutDown();
		}
	}
	
	
	// to test stop updates and alter update mode tasking commands:
	
	/*
	private class SendAlterTaskingCommands implements Runnable {
		
		public void run() {
			
			// Send "alter update mode" tasking commands
			
			HttpClient client = vertx.createHttpClient();
			String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.SENSING_ROUTE
					+ "/tasking_command/";
			
			// First, we need to convert the objects to RDF statements
			// We use the repository for this
			Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			RepositoryConnection conn = repo.getConnection();
			RDFBeanManager manager = new RDFBeanManager(conn);
			
			// Prepare to write the RDF statements
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			RepositoryResult<Statement> iter = null;
			
			for(AssertionCapability ac : assertionCapabilities.values()) {
				
				AlterUpdateModeCommand taskingCommand = new AlterUpdateModeCommand();
				taskingCommand.setTargetAgent(ac.getProvider());
				taskingCommand.setTargetAssertion(ac.getContent());
				AssertionUpdateMode updateMode = new AssertionUpdateMode();
				updateMode.setUpdateMode(AssertionUpdateMode.TIME_BASED);
				updateMode.setUpdateRate(100);
				taskingCommand.setUpdateMode(updateMode);

				writer.startRDF();
				
				try {
					manager.add(taskingCommand);
				} catch (RepositoryException | RDFBeanException e) {
					e.printStackTrace();
				}
				
				// Write the statements
				iter = conn.getStatements(null, null, null);
				while(iter.hasNext()) {
					writer.handleStatement(iter.next());
				}
				
				writer.endRDF();
				
				AgentAddress address = ac.getProvider().getAddress();
				
				client.put(address.getPort(), address.getIpAddress(), route, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						if(resp.statusCode() != 200) {
							System.err.println("CtxSensor at " + address.getIpAddress() + ":" + address.getPort()
								+ " could not stop updates: " + resp.statusCode() + " " + resp.statusMessage());
						}
					}
				}).end(baos.toString());
				
				baos.reset();
				conn.clear();
			}
			
			conn.close();
			repo.shutDown();
		}
	}
	
	private class SendStopTaskingCommands implements Runnable {
		
		public void run() {
			
			// Send "stop updates" tasking commands
			
			HttpClient client = vertx.createHttpClient();
			String route = RouteConfig.API_ROUTE + RouteConfigV1.VERSION_ROUTE + RouteConfig.SENSING_ROUTE
					+ "/tasking_command/";
			
			// First, we need to convert the objects to RDF statements
			// We use the repository for this
			Repository repo = new SailRepository(new MemoryStore());
			repo.initialize();
			RepositoryConnection conn = repo.getConnection();
			RDFBeanManager manager = new RDFBeanManager(conn);
			
			// Prepare to write the RDF statements
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			RepositoryResult<Statement> iter = null;
			
			for(AssertionCapability ac : assertionCapabilities.values()) {
				
				StopUpdatesCommand taskingCommand = new StopUpdatesCommand();
				taskingCommand.setTargetAgent(ac.getProvider());
				taskingCommand.setTargetAssertion(ac.getContent());

				writer.startRDF();
				
				try {
					manager.add(taskingCommand);
				} catch (RepositoryException | RDFBeanException e) {
					e.printStackTrace();
				}
				
				// Write the statements
				iter = conn.getStatements(null, null, null);
				while(iter.hasNext()) {
					writer.handleStatement(iter.next());
				}
				
				writer.endRDF();
				
				AgentAddress address = ac.getProvider().getAddress();
				
				client.put(address.getPort(), address.getIpAddress(), route, new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {
						if(resp.statusCode() != 200) {
							System.err.println("CtxSensor at " + address.getIpAddress() + ":" + address.getPort()
								+ " could not stop updates: " + resp.statusCode() + " " + resp.statusMessage());
						}
					}
				}).end(baos.toString());
				
				baos.reset();
				conn.clear();
			}
			
			conn.close();
			repo.shutDown();
		}
	}*/
}
