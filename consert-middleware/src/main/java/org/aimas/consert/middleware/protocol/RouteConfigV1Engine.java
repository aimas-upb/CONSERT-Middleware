package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.consert.middleware.agents.ConsertEngine;
import org.aimas.consert.model.content.ContextAssertion;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CONSERT Engine in version 1
 */
public class RouteConfigV1Engine {
	
	private ConsertEngine engine;  // the engine that can be accessed with the defined routes
	
	private Repository convRepo;  // repository used for the conversion between Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // the connection to the conversion repository

	
	public RouteConfigV1Engine(ConsertEngine engine) {
		this.engine = engine;
		
		this.convRepo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
	}
	
	
	/**
	 * POST insert context assertion
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleInsertEvent(RoutingContext rtCtx) {

		String rdf = rtCtx.getBodyAsString();
		
		// Insert the two graphs in the conversion repository
		try {

			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TRIG);
			this.convRepoConn.add(model);

			Resource assertG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#assertionGraph");
			IRI bindingClass = SimpleValueFactory.getInstance()
					.createIRI("http://viceversatech.com/rdfbeans/2.0/bindingClass");
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			
			// Parsing the binding classes from default graph
			Map<Resource, Class<?>> bindingClasses = new HashMap<Resource, Class<?>>();
			
			RepositoryResult<Statement> bindingStatements = this.convRepoConn.getStatements(null, bindingClass, null);
			
			while(bindingStatements.hasNext()) {
				Statement s = bindingStatements.next();
				bindingClasses.put(s.getSubject(), Class.forName(s.getObject().stringValue()));
			}
			
			// Parsing the assertions graph
			RDFBeanManager manager = new RDFBeanManager(this.convRepoConn);
			List<ContextAssertion> contextAssertions = new LinkedList<ContextAssertion>();
			
			RepositoryResult<Statement> assertionsStatements = this.convRepoConn.getStatements(null, rdfType, null,
					assertG);
			
			while(assertionsStatements.hasNext()) {
				
				Statement s = assertionsStatements.next();
				
				try {
					
					ContextAssertion ca = (ContextAssertion) manager.get(s.getSubject(), bindingClasses.get(s.getObject()));
					ca.setProcessingTimeStamp(System.currentTimeMillis());
					ca.setAssertionIdentifier(s.getSubject().stringValue());
					contextAssertions.add(ca);
					
				} catch(ClassCastException e) {
					continue;
				}
			}
			
			// Insertion in CONSERT Engine
			for(ContextAssertion ca : contextAssertions) {
				this.engine.insertEvent(ca);
			}
			
			this.convRepoConn.clear();
			
			rtCtx.response().setStatusCode(201).end();
			
		} catch (Exception e) {

			this.convRepoConn.clear();
			System.err.println("Error while creating new ContextAssertion: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}
	
	/**
	 * GET answer query
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetAnswerQuery(RoutingContext rtCtx) {
		
		// Prepare the query
		RepositoryConnection conn = this.engine.getRepository().getConnection();
		TupleQuery query = conn.prepareTupleQuery(rtCtx.getBodyAsString());
		
		// Execute the query and get the result in JSON to send it in the HTTP response
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(baos);
		query.evaluate(writer);
		
		// Send the result
		rtCtx.response().setStatusCode(200).putHeader("content-type", "application/json").end(baos.toString());
		
		conn.close();
	}
}
