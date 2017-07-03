package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.aimas.consert.middleware.agents.Agent;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.ext.web.RoutingContext;

public class RouteUtils {

	/**
	 * Generic POST handler
	 * @param rtCtx the routing context
	 * @param rdfClassName the full URI of the RDF class that the inserted instance is from
	 * @param javaClass the Java class related to the given RDF class name
	 * @param agent the agent containing the repository that stores the RDF triples
	 */
	public static List<Entry<UUID, Object>> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass,
			Agent agent) {
		
		// Initialization
		String rdf = rtCtx.getBodyAsString();
		UUID uuid = UUID.randomUUID();
		List<Entry<UUID, Object>> list = new LinkedList<Entry<UUID, Object>>();

		RepositoryConnection conn = agent.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			// Getting the object we just inserted
			for(Statement s : model) {
				if(s.getObject().stringValue().equals(rdfClassName)) {
					
					list.add(new SimpleEntry<UUID, Object>(uuid, manager.get(s.getSubject(), javaClass)));
					
					break;
				}
			}

			conn.close();

			// Answer by giving the UUID associated to the AssertionCapability
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
		
		return list;
	}
}
