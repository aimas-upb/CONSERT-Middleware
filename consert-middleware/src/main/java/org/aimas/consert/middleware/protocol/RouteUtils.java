package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.Agent;
import org.aimas.consert.middleware.model.RDFObject;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.ext.web.RoutingContext;

/**
 * Generic methods for REST calls
 */
public class RouteUtils {

	/**
	 * Generic POST handler
	 * @param rtCtx the routing context
	 * @param rdfClassName the full URI of the RDF class that the inserted instance is from
	 * @param javaClass the Java class related to the given RDF class name
	 * @param agent the agent containing the repository that stores the RDF triples
	 */
	public static Entry<UUID, Object> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass,
			Agent agent) {
		
		// Initialization
		String rdf = rtCtx.getBodyAsString();
		UUID uuid = UUID.randomUUID();
		Entry<UUID, Object> inserted = null;

		RepositoryConnection conn = agent.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			// Getting the object we just inserted
			for(Statement s : model) {
				if(s.getObject().stringValue().equals(rdfClassName)) {
					
					inserted = new SimpleEntry<UUID, Object>(uuid, manager.get(s.getSubject(), javaClass));
					
					break;
				}
			}

			conn.close();

			// Answer by giving the UUID associated to the inserted object
			rtCtx.response()
				.setStatusCode(201)
				.putHeader("content-type", "text/plain")
				.end(uuid.toString());
		} catch (RDF4JException | RDFBeanException | IOException e) {

			conn.close();
			System.err.println("Error while creating new " + rdfClassName + ": " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
		
		return inserted;
	}
	
	
	/**
	 * Generic GET handler
	 * @param rtCtx the routing context
	 * @param javaClass the Java class related to the given RDF class name
	 * @param agent the agent containing the repository that stores the RDF triples
	 * @param obj the java object corresponding to the RDF statements to fetch
	 */
	public static void get(RoutingContext rtCtx, Class<?> javaClass, Agent agent, RDFObject obj) {
		
		if(obj != null) {

			// Connection to repository to get all the statements
			RepositoryConnection conn = agent.getRepository().getConnection();
			RDFBeanManager manager = new RDFBeanManager(conn);
			
			// Prepare to write RDF statements
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
			writer.startRDF();
			
			try {

				// Get all the statements corresponding to the given object (as the subject)
				Resource objRes = manager.getResource(obj.getId(), javaClass);

				RepositoryResult<Statement> iter = conn.getStatements(objRes, null, null);
				
				// Write all the statements
				while(iter.hasNext()) {
					
					writer.handleStatement(iter.next());
				}

				conn.close();
				
			} catch (RepositoryException | RDFBeanException e) {

				conn.close();
				System.err.println("Error while getting information for object " + obj.getId());
				e.printStackTrace();
				rtCtx.response().setStatusCode(500).end();
			}
			
			writer.endRDF();

			// Answer with the RDF statements
			rtCtx.response()
				.setStatusCode(200)
				.putHeader("content-type", "text/turtle")
				.end(baos.toString());
			
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}
}
