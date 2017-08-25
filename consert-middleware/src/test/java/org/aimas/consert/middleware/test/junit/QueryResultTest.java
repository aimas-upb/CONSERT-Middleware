package org.aimas.consert.middleware.test.junit;

import java.util.List;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.protocol.RequestResource;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for query results
 */
public class QueryResultTest {
	
	private static final String QUERY = 
			  "PREFIX protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#>\n"
			+ "SELECT ?addr ?ip ?port \n"
			+ "WHERE {\n"
			+ "    ?addr a protocol:AgentAddress .\n"
			+ "    ?addr protocol:ipAddress ?ip .\n"
			+ "    ?addr protocol:port ?port .\n"
			+ "}\n";
	
	private Repository repo;
	private RepositoryConnection conn;
	private RDFBeanManager manager;
	
	@Before
	public void setUp() {

		this.repo = new SailRepository(new MemoryStore());
		this.repo.initialize();
		this.conn = this.repo.getConnection();
		this.manager = new RDFBeanManager(this.conn);
	}

	@After
	public void tearDown() {
		this.conn.clear();
		this.conn.close();
		this.repo.shutDown();
	}

	@Test
	public void testIsUpdatedEqual() {
		
		// See if the results of the same query executed twice on the same data are equal
		
		// Prepare the data
		AgentAddress addr = new AgentAddress();
		addr.setId("http://id1.com");
		addr.setIpAddress("127.0.0.1");
		addr.setPort(80);
		
		// Insert the data in the repository
		try {
			this.manager.add(addr);
		} catch (RepositoryException | RDFBeanException e) {
			Assert.fail("Failed to add resource in repository: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Make the first query
		TupleQuery query1 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res1 = QueryResults.asList(query1.evaluate());
		RequestResource r1 = new RequestResource();
		r1.setResult(res1);
		
		// Make the second query, on the same data
		TupleQuery query2 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res2 = QueryResults.asList(query2.evaluate());
		
		Assert.assertFalse(r1.hasResultChanged(res2));
	}

	@Test
	public void testIsUpdatedSize() {
		
		// See if the results of the same query executed twice on different size data are different
		
		// Prepare the data for the first query
		AgentAddress addr1 = new AgentAddress();
		addr1.setId("http://id1.com");
		addr1.setIpAddress("127.0.0.1");
		addr1.setPort(80);
		
		// Insert the data for the first query in the repository
		try {
			this.manager.add(addr1);
		} catch (RepositoryException | RDFBeanException e) {
			Assert.fail("Failed to add resource in repository: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Make the first query
		TupleQuery query1 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res1 = QueryResults.asList(query1.evaluate());
		RequestResource r1 = new RequestResource();
		r1.setResult(res1);
		
		
		// Prepare the data for the second query
		AgentAddress addr2 = new AgentAddress();
		addr2.setId("http://id2.com");
		addr2.setIpAddress("127.0.0.1");
		addr2.setPort(80);
		
		// Insert the data for the second query in the repository, with the already inserted data
		try {
			this.manager.add(addr2);
		} catch (RepositoryException | RDFBeanException e) {
			Assert.fail("Failed to add resource in repository: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Make the second query
		TupleQuery query2 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res2 = QueryResults.asList(query2.evaluate());
		
		Assert.assertTrue(r1.hasResultChanged(res2));
	}

	@Test
	public void testIsUpdatedElement() {
		
		// See if the results of the same query executed twice on same size data but with different content are different
		
		// Prepare the data for the first query
		AgentAddress addr = new AgentAddress();
		addr.setId("http://id1.com");
		addr.setIpAddress("127.0.0.1");
		addr.setPort(80);
		
		// Insert the data for the first query in the repository
		try {
			this.manager.add(addr);
		} catch (RepositoryException | RDFBeanException e) {
			Assert.fail("Failed to add resource in repository: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Make the first query
		TupleQuery query1 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res1 = QueryResults.asList(query1.evaluate());
		RequestResource r1 = new RequestResource();
		r1.setResult(res1);
		
		
		// Update the existing data to use a different value
		addr.setPort(8080);
		
		// Update the inserted data in the repository
		try {
			this.manager.update(addr);
		} catch (RepositoryException | RDFBeanException e) {
			Assert.fail("Failed to add resource in repository: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Make the second query, using the updated data
		TupleQuery query2 = conn.prepareTupleQuery(QueryResultTest.QUERY);
		List<BindingSet> res2 = QueryResults.asList(query2.evaluate());
		
		Assert.assertTrue(r1.hasResultChanged(res2));
	}
}