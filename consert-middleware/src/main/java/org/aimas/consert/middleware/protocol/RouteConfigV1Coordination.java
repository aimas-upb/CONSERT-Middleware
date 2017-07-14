package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.aimas.consert.middleware.model.AssertionInstance;
import org.aimas.consert.middleware.model.RDFObject;
import org.aimas.consert.model.annotations.ContextAnnotation;
import org.aimas.consert.model.content.ContextEntity;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
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

	public RouteConfigV1Coordination(CtxCoord ctxCoord) {
		this.ctxCoord = ctxCoord;
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
		
		// Create a Repository that will contain the two graphs
		Repository repo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		
		try {

			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TRIG);
			conn.add(model);

			Resource assertG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#assertionGraph");
			Resource annG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#annotationGraph");
			IRI bindingClass = SimpleValueFactory.getInstance()
					.createIRI("http://viceversatech.com/rdfbeans/2.0/bindingClass");
			IRI rdfType = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			IRI hasAnnotation = SimpleValueFactory.getInstance().createIRI(
					"http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#hasAnnotation");
			
			// Parsing the binding classes from default graph
			Map<Resource, Class<?>> bindingClasses = new HashMap<Resource, Class<?>>();
			
			RepositoryResult<Statement> bindingStatements = conn.getStatements(null, bindingClass, null);
			
			while(bindingStatements.hasNext()) {
				Statement s = bindingStatements.next();
				bindingClasses.put(s.getSubject(), Class.forName(s.getObject().stringValue()));
			}
			
			// Parsing the assertions graph
			List<AssertionInstance> assertionInstances = new LinkedList<AssertionInstance>();
			
			RepositoryResult<Statement> assertionsStatements = conn.getStatements(null, rdfType, null, assertG);
			
			while(assertionsStatements.hasNext()) {
				Statement s = assertionsStatements.next();
				
				AssertionInstance ai = new AssertionInstance();
				ai.setId(s.getSubject().stringValue());
				assertionInstances.add(ai);
			}
			
			// Parsing the annotations graph
			Map<Resource, Class<?>> annotationsClasses = new HashMap<Resource, Class<?>>();
			
			RepositoryResult<Statement> annotationsStatements = conn.getStatements(null, rdfType, null, annG);
			
			while(annotationsStatements.hasNext()) {
				Statement s = annotationsStatements.next();
				annotationsClasses.put(s.getSubject(), bindingClasses.get(s.getObject()));
			}
			
			// Link annotations to assertions
			RDFBeanManager manager = new RDFBeanManager(conn);
			RepositoryResult<Statement> hasAnnotationStatements = conn.getStatements(null, hasAnnotation, null, annG);
			
			while(hasAnnotationStatements.hasNext()) {
				Statement s = hasAnnotationStatements.next();
				
				for(AssertionInstance ai : assertionInstances) {
					if(ai.getId().equals(s.getSubject().stringValue())) {
						Class<?> annClass = annotationsClasses.get(s.getObject());						
						ContextAnnotation ca = (ContextAnnotation) manager.get(s.getObject().stringValue(), annClass);
						ai.addAnnotation(ca);
					}
				}
			}
			
			// Display
			for(AssertionInstance ai : assertionInstances) {
				System.out.println(ai.getId() + ":");
				for(ContextAnnotation ca : ai.getAnnotations()) {
					System.out.println("\t" + ca.getAnnotationIdentifier());
				}
			}
			
			// TODO implement processing of AssertionInstances
			
		} catch (Exception e) {

			conn.close();
			repo.shutDown();
			System.err.println("Error while creating new ContextAssertion instance: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(500).end();
		}
		
		conn.close();
		repo.shutDown();
		
		rtCtx.response().setStatusCode(201).end();
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
		// TODO
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
