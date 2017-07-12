package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
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

		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n\n"
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

			Resource res = manager.getResource(
					"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/Address",
					AgentAddress.class);
			AgentAddress real = (AgentAddress) manager.get(res, AgentAddress.class);

			conn.close();
			repo.shutDown();

			Assert.assertEquals(expected, real);

		} catch (RDFParseException | UnsupportedRDFormatException | IOException | RepositoryException
				| RDFBeanException e) {

			conn.close();
			repo.shutDown();
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testRDFToObjectComplex() {

		AgentAddress addr = new AgentAddress();
		addr.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/Address");
		addr.setIpAddress("127.0.0.1");
		addr.setPort(8080);
		AgentSpec expected = new AgentSpec();
		expected.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/Spec");
		expected.setIdentifier("TestIdentifier");
		expected.setAddress(addr);

		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n"
				+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
				+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n\n"
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

			Resource res = manager.getResource(
					"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/Spec", AgentSpec.class);
			AgentSpec real = (AgentSpec) manager.get(res, AgentSpec.class);

			Assert.assertEquals(expected, real);
			Assert.assertEquals(addr, real.getAddress());

		} catch (RDFParseException | UnsupportedRDFormatException | IOException | RepositoryException
				| RDFBeanException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testInferredContextAnnotations() {

		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#> .\n"
				+ "@prefix context-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/> .\n"
				+ "@prefix assertion-capability: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/> .\n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
				+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
				+ "annotation:ContextAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.ContextAnnotation\"^^xsd:string .\n"
				+ "annotation:BasicAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.BasicAnnotation\"^^xsd:string .\n"
				+ "annotation:StructuredAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.StructuredAnnotation\"^^xsd:string .\n"
				+ "annotation:TimestampAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.TimestampAnnotation\"^^xsd:string .\n"
				+ "annotation:CertaintyAnnotation rdfbeans:bindingClass \"org.aimas.consert.middleware.model.CertaintyAnnotation\"^^xsd:string .\n"
				+ "protocol:AssertionCapability rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionCapability\"^^xsd:string .\n\n"
				+ "assertion-capability:AssertCap a protocol:AssertionCapability ;\n"
				+ "    annotation:hasAnnotation context-annotation:tsann1 ;\n"
				+ "    annotation:hasAnnotation context-annotation:certann1 .\n"
				+ "assertion-capability:AssertCap2 a protocol:AssertionCapability ;\n"
				+ "    annotation:hasAnnotation context-annotation:tsann2 ;\n"
				+ "    annotation:hasAnnotation context-annotation:basicann1 .\n"
				+ "context-annotation:tsann1 a annotation:TimestampAnnotation .\n"
				+ "context-annotation:certann1 a annotation:CertaintyAnnotation .\n"
				+ "context-annotation:tsann2 a annotation:TimestampAnnotation .\n"
				+ "context-annotation:basicann1 a annotation:BasicAnnotation .\n"
				+ "annotation:TimestampAnnotation rdfs:subClassOf annotation:StructuredAnnotation .\n"
				+ "annotation:CertaintyAnnotation rdfs:subClassOf annotation:StructuredAnnotation .\n"
				+ "annotation:BasicAnnotation rdfs:subClassOf annotation:ContextAnnotation .\n"
				+ "annotation:StructuredAnnotation rdfs:subClassOf annotation:ContextAnnotation .\n";

		String expected = "(http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/certann1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#StructuredAnnotation) [null]"
				+ "(http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/tsann1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#StructuredAnnotation) [null]"
				+ "(http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#ContextAnnotation/tsann2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#StructuredAnnotation) [null]";

		Repository repo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();

		try {

			Model model = Rio.parse(new ByteArrayInputStream(rdfData.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			IRI ann = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#StructuredAnnotation");
			IRI type = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

			RepositoryResult<Statement> iter = conn.getStatements(null, type, ann);
			StringBuilder sb = new StringBuilder();

			while (iter.hasNext()) {
				Statement s = iter.next();
				// System.out.println(s);
				sb.append(s);
			}

			conn.close();
			repo.shutDown();

			Assert.assertEquals(expected, sb.toString());

		} catch (Exception e) {

			conn.close();
			repo.shutDown();
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGraphs() {

		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#> .\n"
				+ "@prefix assertion-instance: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionInstance> .\n"
				+ "@prefix timestamp-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#TimestampAnnotation> .\n"
				+ "@prefix certainty-annotation: <http://pervasive.semanticweb.org/ont/2017/07/consert/annotation#CertaintyAnnotation> .\n\n"
				+ "protocol:assertionGraph {\n"
				+ "    assertion-instance:assertionUpdate a protocol:AssertionInstance ;\n"
				+ "        protocol:hasContent assertion-instance:assertionContent .\n"
				+ "}\n"
				+ "protocol:annotationGraph {\n"
				+ "    assertion-instance:assertionUpdate annotation:hasAnnotation timestamp-annotation:ts .\n"
				+ "    timestamp-annotation:ts a annotation:TimestampAnnotation .\n"
				+ "    certainty-annotation:certainty a annotation:CertaintyAnnotation .\n"
				+ "}\n";

		Repository repo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();

		Model model;
		try {

			model = Rio.parse(new ByteArrayInputStream(rdfData.getBytes()), "", RDFFormat.TRIG);
			conn.add(model);

			Resource assertG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#assertionGraph");
			Resource annG = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#annotationGraph");

			StringBuilder sbAssert = new StringBuilder();
			StringBuilder sbAnn = new StringBuilder();

			RepositoryResult<Statement> iter = conn.getStatements(null, null, null, assertG);

			while (iter.hasNext()) {
				Statement s = iter.next();
				sbAssert.append(s);
				// System.out.println(s);
			}

			iter = conn.getStatements(null, null, null, annG);

			while (iter.hasNext()) {
				Statement s = iter.next();
				sbAnn.append(s);
				// System.out.println(s);
			}

			conn.close();
			repo.shutDown();

			boolean contained = sbAssert.toString().contains("assertionGraph")
					&& sbAnn.toString().contains("annotationGraph");
			boolean separated = !(sbAssert.toString().contains("annotationGraph")
					|| sbAnn.toString().contains("assertionGraph"));

			Assert.assertTrue(contained && separated);

		} catch (RDFParseException | UnsupportedRDFormatException | IOException e) {

			conn.close();
			repo.shutDown();
			e.printStackTrace();
			Assert.fail();
		}
	}
}
