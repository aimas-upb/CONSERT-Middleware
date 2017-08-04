package org.aimas.consert.middleware.protocol;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionCapabilitySubscription;
import org.aimas.consert.middleware.model.RDFObject;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.model.content.ContextEntity;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxCoord agent in version 1
 */
public class RouteConfigV1Coordination extends RouteConfigV1 {

	private CtxCoord ctxCoord; // the agent that can be accessed with the
								// defined routes
	
	private Repository convRepo;  // repository used for the conversion between Java objects and RDF statements
	private RepositoryConnection convRepoConn;  // the connection to the conversion repository

	public RouteConfigV1Coordination(CtxCoord ctxCoord) {
		this.ctxCoord = ctxCoord;
		
		this.convRepo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		this.convRepo.initialize();
		this.convRepoConn = this.convRepo.getConnection();
	}

	/**
	 * POST publish assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostCtxAsserts(RoutingContext rtCtx) {

		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability",
				AssertionCapability.class);

		// Insertion in CtxCoord
		AssertionCapability ac = (AssertionCapability) entry.getValue();
		this.ctxCoord.addAssertionCapability(entry.getKey(), ac);
	}

	/**
	 * GET list assertion capabilities
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAsserts(RoutingContext rtCtx) {

		// Get agent identifier from query
		String agent = rtCtx.request().getParam("agentIdentifier");

		// Get all known AssertionCapabilities
		Collection<AssertionCapability> acs = this.ctxCoord.getAssertionCapabilitiesValues();

		// Connection to repository to get the provider of each
		// AssertionCapability and the statements
		RepositoryConnection conn = this.ctxCoord.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		// Prepare to write RDF statements
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
		writer.startRDF();

		// For each AssertionCapability from CtxCoord, fetch the AgentSpec
		// provider.
		// If the provider is the requested one, write all the statements where
		// the AssertionCapability is the subject.
		for (AssertionCapability ac : acs) {

			// Check the provider
			AgentSpec as = ac.getProvider();

			if (as.getIdentifier().equals(agent)) {

				try {

					// Get all the statements corresponding to the
					// AssertionCapability (as the subject)
					Resource acRes = manager.getResource(ac.getId(), AssertionCapability.class);

					RepositoryResult<Statement> iter = conn.getStatements(acRes, null, null);

					// Write all the statements
					while (iter.hasNext()) {
						writer.handleStatement(iter.next());
					}

				} catch (RepositoryException | RDFBeanException e) {

					System.err.println("Error while getting information for AssertionCapability " + ac.getId());
					e.printStackTrace();
					rtCtx.response().setStatusCode(500).end();
				}
			}
		}

		conn.close();
		writer.endRDF();

		// Answer with the RDF statements
		rtCtx.response().setStatusCode(200).putHeader("content-type", "text/turtle").end(baos.toString());
	}

	/**
	 * GET list assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetCtxAssert(RoutingContext rtCtx) {

		// Get UUID of the AssertionCapability from query
		UUID uuid = UUID.fromString(rtCtx.request().getParam("id"));

		// Get the corresponding AssertionCapability
		AssertionCapability ac = this.ctxCoord.getAssertionCapability(uuid);

		this.get(rtCtx, AssertionCapability.class, ac);
	}

	/**
	 * PUT update assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutCtxAssert(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");
		String resourceId = this.ctxCoord.getAssertionCapability(UUID.fromString(uuid)).getId();

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability",
				AssertionCapability.class, resourceId);

		if (entry != null) {

			// Remove old AssertionCapability from CtxCoord
			AssertionCapability ac = this.ctxCoord.removeAssertionCapability(UUID.fromString(uuid));
			if (ac == null) {
				rtCtx.response().setStatusCode(404).end();
				return;
			}

			AssertionCapability newAc = (AssertionCapability) entry.getValue();

			// Insertion in CtxCoord
			this.ctxCoord.addAssertionCapability(UUID.fromString(uuid), newAc);
		}
	}

	/**
	 * DELETE delete assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCtxAssert(RoutingContext rtCtx) {
		
		// Initialization
		String uuid = rtCtx.request().getParam("id");

		// Remove old AssertionCapability from the repository
		String resourceId = this.ctxCoord.getAssertionCapability(UUID.fromString(uuid)).getId();

		boolean done = this.delete(rtCtx, AssertionCapability.class, resourceId);

		if (done) {

			// Remove old AssertionCapability from CtxCoord
			AssertionCapability ac = this.ctxCoord.removeAssertionCapability(UUID.fromString(uuid));
			if (ac == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
		
		// Stop CtxCoord if there is no more data to receive
		if(this.ctxCoord.getAssertionCapabilitiesValues().isEmpty()) {
			
			this.ctxCoord.getVertx().executeBlocking(future -> {
				
				this.ctxCoord.stopVertx();
				future.complete();
				
			}, null);
		}
	}

	/**
	 * POST subscribe for assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostAssertCapSubs(RoutingContext rtCtx) {

		Entry<UUID, Object> entry = this.post(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription",
				AssertionCapabilitySubscription.class);

		// Insertion in CtxCoord
		AssertionCapabilitySubscription acs = (AssertionCapabilitySubscription) entry.getValue();
		this.ctxCoord.addAssertionCapabilitySubscription(entry.getKey(), acs);
	}

	/**
	 * GET inspect assertion capability subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetAssertCapSub(RoutingContext rtCtx) {

		// Get UUID of the object from query
		UUID uuid = UUID.fromString(rtCtx.request().getParam("id"));

		// Get the corresponding object
		AssertionCapabilitySubscription acs = this.ctxCoord.getAssertionCapabilitySubscription(uuid);

		this.get(rtCtx, AssertionCapabilitySubscription.class, acs);
	}

	/**
	 * PUT update assertion capability subscription
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutAssertCapSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");
		String resourceId = this.ctxCoord.getAssertionCapabilitySubscription(UUID.fromString(uuid)).getId();

		Entry<UUID, Object> entry = this.put(rtCtx,
				"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription",
				AssertionCapabilitySubscription.class, resourceId);

		if (entry != null) {

			// Remove old AssertionCapabilitySubscription from CtxCoord
			AssertionCapabilitySubscription acs = this.ctxCoord
					.removeAssertionCapabilitySubscription(UUID.fromString(uuid));

			if (acs == null) {
				rtCtx.response().setStatusCode(404).end();
				return;
			}

			AssertionCapabilitySubscription newAc = (AssertionCapabilitySubscription) entry.getValue();

			// Insertion in CtxCoord
			this.ctxCoord.addAssertionCapabilitySubscription(UUID.fromString(uuid), newAc);
		}
	}

	/**
	 * DELETE unsubscribe for assertion capability
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleDeleteCapSub(RoutingContext rtCtx) {

		// Initialization
		String uuid = rtCtx.request().getParam("id");

		// Remove old AssertionCapability from the repository
		String resourceId = this.ctxCoord.getAssertionCapabilitySubscription(UUID.fromString(uuid)).getId();

		boolean done = this.delete(rtCtx, AssertionCapabilitySubscription.class, resourceId);

		if (done) {

			// Remove old AssertionCapabilitySubscription from CtxCoord
			AssertionCapabilitySubscription acs = this.ctxCoord
					.removeAssertionCapabilitySubscription(UUID.fromString(uuid));
			if (acs == null) {
				rtCtx.response().setStatusCode(404).end();
			}
		} else {
			rtCtx.response().setStatusCode(404).end();
		}
	}

	/**
	 * POST create ContextAssertion instance
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostInsCtxAssert(RoutingContext rtCtx) {
		
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
			
			// Insert all the statements in the engine repository
			RepositoryConnection engineConn = this.ctxCoord.getEngineRepository().getConnection();
			RepositoryResult<Statement> allStatements = this.convRepoConn.getStatements(null, null, null);
			engineConn.add(allStatements);
			
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
				this.ctxCoord.insertEvent(ca);
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
	 * POST static context insertion
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostInsEntityDescs(RoutingContext rtCtx) {
		
		// Initialization
		String rdf = rtCtx.getBodyAsString();

		Repository tmpRep = new SailRepository(new MemoryStore());
		tmpRep.initialize();
		RepositoryConnection conn = tmpRep.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);
			
			
			
			// Get all the binding classes
			IRI bindingIRI = SimpleValueFactory.getInstance()
					.createIRI("http://viceversatech.com/rdfbeans/2.0/bindingClass");
			Map<Value, Class<?>> bindingClasses = new HashMap<Value, Class<?>>();
			RepositoryResult<Statement> iter = conn.getStatements(null, bindingIRI, null);
			
			while(iter.hasNext()) {
				Statement s = iter.next();
				bindingClasses.put(s.getSubject(), Class.forName(s.getObject().stringValue()));
			}
			
			// Get the ContextEntity instances
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			List<ContextEntity> entities = new LinkedList<ContextEntity>();
			iter = conn.getStatements(null, rdfType, null);
			
			while(iter.hasNext()) {
				Statement s = iter.next();
				Class<?> c = bindingClasses.get(s.getObject());
					
				Class<?>[] ifaces = c.getInterfaces();
				
				for(Class<?> iface : ifaces) {
					if(iface == ContextEntity.class) {
						entities.add((ContextEntity) manager.get(s.getSubject()));
					}
				}
			}
			
			// Display
			/*for(ContextEntity ce : entities) {
				System.out.println(ce);
			}*/

			conn.close();
			tmpRep.shutDown();

			// Answer with a status code only
			rtCtx.response().setStatusCode(200).end();
		} catch (Exception e) {

			conn.close();
			tmpRep.shutDown();
			System.err.println("Error while getting static context insertion: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}

	/**
	 * POST static context update
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostUpdateEntDescs(RoutingContext rtCtx) {
		
		// Initialization
		String rdf = rtCtx.getBodyAsString();

		Repository tmpRep = new SailRepository(new MemoryStore());
		tmpRep.initialize();
		RepositoryConnection conn = tmpRep.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);
			
			
			
			// Get all the binding classes
			IRI bindingIRI = SimpleValueFactory.getInstance()
					.createIRI("http://viceversatech.com/rdfbeans/2.0/bindingClass");
			Map<Value, Class<?>> bindingClasses = new HashMap<Value, Class<?>>();
			RepositoryResult<Statement> iter = conn.getStatements(null, bindingIRI, null);
			
			while(iter.hasNext()) {
				Statement s = iter.next();
				bindingClasses.put(s.getSubject(), Class.forName(s.getObject().stringValue()));
			}
			
			// Get the ContextEntity instances
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			List<ContextEntity> entities = new LinkedList<ContextEntity>();
			iter = conn.getStatements(null, rdfType, null);
			
			while(iter.hasNext()) {
				Statement s = iter.next();
				Class<?> c = bindingClasses.get(s.getObject());
					
				Class<?>[] ifaces = c.getInterfaces();
				
				for(Class<?> iface : ifaces) {
					if(iface == ContextEntity.class) {
						entities.add((ContextEntity) manager.get(s.getSubject()));
					}
				}
			}
			
			// Display
			/*for(ContextEntity ce : entities) {
				System.out.println(ce);
			}*/

			conn.close();
			tmpRep.shutDown();

			// Answer with a status code only
			rtCtx.response().setStatusCode(200).end();
		} catch (Exception e) {

			conn.close();
			tmpRep.shutDown();
			System.err.println("Error while getting static context update: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
	}

	/**
	 * POST activate ContextAssertionInstance
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostActivateCtxAssert(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST register query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostRegQueryHandler(RoutingContext rtCtx) {
		// TODO
	}

	/**
	 * POST unregister query handler
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePostUnregQueryHandler(RoutingContext rtCtx) {
		// TODO
	}
	
	/**
	 * GET answer query
	 * 
	 * @param rtCtx the routing context
	 */
	public void handleGetAnswerQuery(RoutingContext rtCtx) {
		
		// Prepare the query
		RepositoryConnection conn = this.ctxCoord.getEngineRepository().getConnection();
		GraphQuery query = conn.prepareGraphQuery(rtCtx.getBodyAsString());
		
		// Execute the query and write the result in Turtle syntax
		StringWriter sw = new StringWriter();
		RDFHandler writer = Rio.createWriter(RDFFormat.TURTLE, sw);
		query.evaluate(writer);
		sw.flush();		
		
		// Send the result
		rtCtx.response().setStatusCode(200).putHeader("content-type", "text/turtle").end(sw.toString());
		
		conn.close();
	}

	private Entry<UUID, Object> post(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass) {
		return RouteUtils.post(rtCtx, rdfClassName, javaClass, this.ctxCoord);
	}

	private void get(RoutingContext rtCtx, Class<?> javaClass, RDFObject obj) {
		RouteUtils.get(rtCtx, javaClass, this.ctxCoord, obj);
	}

	private Entry<UUID, Object> put(RoutingContext rtCtx, String rdfClassName, Class<?> javaClass, String resourceId) {
		return RouteUtils.put(rtCtx, rdfClassName, javaClass, this.ctxCoord, resourceId);
	}

	private boolean delete(RoutingContext rtCtx, Class<?> javaClass, String resourceId) {
		return RouteUtils.delete(rtCtx, javaClass, this.ctxCoord, resourceId);
	}
}
