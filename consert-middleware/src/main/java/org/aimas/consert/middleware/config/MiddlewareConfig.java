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

public abstract class MiddlewareConfig {

	public static final String AGENT_CONFIG_FILE_NAME = "agent-config.ttl";
	public static final String PLATFORM_CONFIG_FILE_NAME = "platform-config.ttl";
	
	private static Repository convRepo;  // repository used to convert Java objects and RDF statements
	private static RepositoryConnection convRepoConn;  // connection to the conversion repository
	private static RDFBeanManager convManager;  // manager for the conversion
	private static boolean isInitialized = false;  // true if the objects needed for the conversions have been initialized
	
	
	/**
	 * Initializes the objects needed for the conversion between java objects and RDF statements if needed
	 */
	private static void init() {
		if(!MiddlewareConfig.isInitialized) {
			MiddlewareConfig.convRepo = new SailRepository(new MemoryStore());
			MiddlewareConfig.convRepo.initialize();
			MiddlewareConfig.convRepoConn = MiddlewareConfig.convRepo.getConnection();
			MiddlewareConfig.convManager = new RDFBeanManager(MiddlewareConfig.convRepoConn);
			
			MiddlewareConfig.isInitialized = true;
		}
	}
	
	
	/**
	 * Reads the configuration file to get the configuration of all the agents of a given type
	 * @param agentSpecClass the class defining the specifications of the wanted agent type
	 * @param agentSpecURI the URI used in the RDF statements that corresponds to the given class
	 * @return a list of the specifications for all the agents of the given type
	 */
	public static List<AgentSpecification> readAgentConfigList(Class<? extends AgentSpecification> agentSpecClass,
			String agentSpecURI) {
		
		MiddlewareConfig.init();
		
		List<AgentSpecification> as = new ArrayList<AgentSpecification>();
		
		try {
			
			InputStream is = new FileInputStream(MiddlewareConfig.AGENT_CONFIG_FILE_NAME);
			
			Model model = Rio.parse(is, "", RDFFormat.TURTLE);
			MiddlewareConfig.convRepoConn.add(model);

			IRI agentAddress = SimpleValueFactory.getInstance()
					.createIRI(agentSpecURI);
			IRI type = SimpleValueFactory.getInstance().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			
			RepositoryResult<Statement> statements = convRepoConn.getStatements(null, type, agentAddress);
			
			while(statements.hasNext()) {
				Statement s = statements.next();
				as.add(MiddlewareConfig.convManager.get(s.getSubject(), agentSpecClass));
			}
			
		} catch (UnsupportedRDFormatException | IOException | RDF4JException | RDFBeanException e) {
			System.err.println("Error while parsing agent configuration file: " + e.getMessage());
			e.printStackTrace();
		}
		
		return as;
	}
	
	/**
	 * Reads the configuration file to get the configuration of one agent of a given type
	 * @param agentSpecClass the class defining the specifications of the wanted agent type
	 * @param agentSpecURI the URI used in the RDF statements that corresponds to the given class
	 * @return the specifications for an agent of the given type
	 */
	public static AgentSpecification readAgentConfig(Class<? extends AgentSpecification> agentSpecClass,
			String agentSpecURI) {
		
		List<AgentSpecification> specs = MiddlewareConfig.readAgentConfigList(agentSpecClass, agentSpecURI);
		
		if(specs == null) {
			return null;
		} else if(specs.isEmpty()) {
			return null;
		}
		
		return specs.get(0);
	}
}
