package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for RDF features
 */
public class RDFTest {

	@Test
	public void testRDFToObjectSimple() {
		
		AgentAddress expected = new AgentAddress();
		expected.setId("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/Address");
		expected.setIpAddress("127.0.0.1");
		expected.setPort(8080);
		
		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/> .\n\n"
				+ "agent-address:Address a protocol:AgentAddress ;\n"
				+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
				+ "    protocol:port \"8080\"^^xsd:int .\n";
		
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			
			Model model = Rio.parse(new ByteArrayInputStream(rdfData.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);
			
			Resource res = manager.getResource("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/Address", AgentAddress.class);
			AgentAddress real = (AgentAddress) manager.get(res, AgentAddress.class);
			
			Assert.assertEquals(expected, real);
			
		} catch (RDFParseException | UnsupportedRDFormatException | IOException | RepositoryException | RDFBeanException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testRDFToObjectComplex() {
		
		AgentAddress addr = new AgentAddress();
		addr.setId("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/Address");
		addr.setIpAddress("127.0.0.1");
		addr.setPort(8080);
		AgentSpec expected = new AgentSpec();
		expected.setId("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/Spec");
		expected.setIdentifier("TestIdentifier");
		expected.setAddress(addr);
		
		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n"
				+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentAddress/> .\n\n"
				+ "agent-address:Address a protocol:AgentAddress ;\n"
				+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
				+ "    protocol:port \"8080\"^^xsd:int .\n"
				+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n"
				+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
				+ "agent-spec:Spec a protocol:AgentSpec ;\n"
				+ "    protocol:hasAddress agent-address:Address ;\n"
				+ "    protocol:hasIdentifier \"TestIdentifier\"^^xsd:string .\n";
		
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);

		try {
			
			Model model = Rio.parse(new ByteArrayInputStream(rdfData.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);
			
			Resource res = manager.getResource("http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AgentSpec/Spec", AgentSpec.class);
			AgentSpec real = (AgentSpec) manager.get(res, AgentSpec.class);
			
			Assert.assertEquals(expected, real);
			
		} catch (RDFParseException | UnsupportedRDFormatException | IOException | RepositoryException | RDFBeanException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
