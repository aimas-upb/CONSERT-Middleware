package org.aimas.consert.middleware.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
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
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public abstract class Configuration {

	public static final String AGENT_CONFIG_FILE_NAME = "agent-config.ttl";
	public static final String PLATFORM_CONFIG_FILE_NAME = "platform-config.ttl";
	
	
	public static List<AgentAddress> readAgentConfig() {

		List<AgentAddress> results = new ArrayList<AgentAddress>();
		
		Repository convRepo = new SailRepository(new MemoryStore());
		convRepo.initialize();
		RepositoryConnection convRepoConn = convRepo.getConnection();
		RDFBeanManager manager = new RDFBeanManager(convRepoConn);
		
		try {
			
			InputStream is = new FileInputStream(Configuration.AGENT_CONFIG_FILE_NAME);
			
			Model model = Rio.parse(is, "", RDFFormat.TURTLE);
			convRepoConn.add(model);

			IRI agentAddress = SimpleValueFactory.getInstance()
					.createIRI("http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#AgentAddress");
			IRI type = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			
			RepositoryResult<Statement> statements = convRepoConn.getStatements(null, type, agentAddress);
			
			while(statements.hasNext()) {
				Statement s = statements.next();
				AgentAddress addr = manager.get(s.getSubject(), AgentAddress.class);
				results.add(addr);
			}
			
		} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
			System.err.println("Error while parsing agent configuration file: " + e.getMessage());
			e.printStackTrace();
		}
		
		return results;
	}
}
