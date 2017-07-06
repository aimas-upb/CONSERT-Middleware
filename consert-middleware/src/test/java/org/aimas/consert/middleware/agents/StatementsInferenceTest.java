package org.aimas.consert.middleware.agents;

import java.io.ByteArrayInputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for inference when getting statements from a repository
 */
public class StatementsInferenceTest {

	@Test
	public void testContextAnnotations() {
		
		String rdfData = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#> .\n"
				+ "@prefix annotation: <http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#> .\n"
				+ "@prefix context-annotation: <http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#ContextAnnotation/> .\n"
				+ "@prefix assertion-capability: <http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#AssertionCapability/> .\n"
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
		
		String expected = "(http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#ContextAnnotation/certann1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#StructuredAnnotation) [null]"
				+ "(http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#ContextAnnotation/tsann1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#StructuredAnnotation) [null]"
				+ "(http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#ContextAnnotation/tsann2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#StructuredAnnotation) [null]";
		
		Repository repo = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();

		try {
			
			Model model = Rio.parse(new ByteArrayInputStream(rdfData.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			IRI ann = SimpleValueFactory.getInstance().createIRI("http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#StructuredAnnotation");
			IRI type = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

			RepositoryResult<Statement> iter = conn.getStatements(null, type, ann, true);
			StringBuilder sb = new StringBuilder();
			
			while(iter.hasNext()) {
				Statement s = iter.next();
				System.out.println(s);
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
}
