package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines an agent policy according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:AgentPolicy")
public class AgentPolicy {
	
	/**
	 * The filename or URI of the document that holds the agent specific control policy constructs.
	 */
	private String fileNameOrURI;
	
	
	public AgentPolicy() {}
	
	public AgentPolicy(String fileNameOrURI) {
	    this.fileNameOrURI = fileNameOrURI;
    }
	
	/**
	 * @return The filename or URI of the document that holds the agent specific control policy constructs.
	 */
    public String getFileNameOrURI() {
	    return fileNameOrURI;
    }
}
