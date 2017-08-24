package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the configuration of an agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:AgentAddress")
public class AgentAddress {
	
	private String agentLocalName;
	private String agentAppIdentifier;
	
	private CMMAgentContainer agentContainer;
	
	
	public AgentAddress() {}
	
	public AgentAddress(String agentLocalName, CMMAgentContainer agentContainer, String agentAppIdentifier) {
	    this.agentLocalName = agentLocalName;
	    
	    this.agentContainer = agentContainer;
	    this.agentAppIdentifier = agentAppIdentifier;
	}
	
	@RDF("orgconf:agentName")
	public String getLocalName() {
		return agentLocalName + "__" + agentAppIdentifier;
	}
	
	public void setLocalName(String agentLocalName) {
		this.agentLocalName = agentLocalName;
	}
	
	@RDF("orgconf:agentAppIdentifier")
	public String getAppIdentifier() {
		return agentAppIdentifier;
	}
	
	public void setAppIdentifier(String agentAppIdentifier) {
		this.agentAppIdentifier = agentAppIdentifier;
	}
	
	@RDF("orgconf:agentContainer")
	public CMMAgentContainer getAgentContainer() {
	    return agentContainer;
    }
	
	public void setAgentContainer(CMMAgentContainer agentContainer) {
		this.agentContainer = agentContainer;
	}
}
