package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the specification of an agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:AgentSpec")
public class AgentSpecification {
	
	protected AgentAddress agentAddress;
	protected AgentAddress assignedManagerAddress;
	protected AgentPolicy controlPolicy;
	
	
	public AgentSpecification() {}
	
	public AgentSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress assignedManagerAddress) {
	    this.agentAddress = agentAddress;
	    this.controlPolicy = controlPolicy;
	    this.assignedManagerAddress = assignedManagerAddress;
    }
	
	@RDF("orgconf:hasAgentAddress")
	public AgentAddress getAgentAddress() {
		return agentAddress;
	}
	
	public void setAgentAddress(AgentAddress agentAddress) {
		this.agentAddress = agentAddress;
	}
	
	public String getAgentLocalName() {
		return agentAddress.getLocalName();
	}
	
	@RDF("orgconf:hasControlPolicy")
	public AgentPolicy getControlPolicy() {
		return controlPolicy;
	}
	
	public void setControlPolicy(AgentPolicy controlPolicy) {
		this.controlPolicy = controlPolicy;
	}
	
	public boolean hasControlPolicy() {
		return controlPolicy != null;
	}
	
	public AgentAddress getAssignedManagerAddress() {
		return assignedManagerAddress;
	}
	
	public boolean hasAssignedManagerAddress() {
		return assignedManagerAddress != null;
	}
	
	@Override
    public int hashCode() {
	    return agentAddress.hashCode();
    }
	
	@Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    
	    if (obj == null) {
		    return false;
	    }
	    
	    if (!(obj instanceof AgentSpecification)) {
		    return false;
	    }
	    
	    AgentSpecification other = (AgentSpecification) obj;
	    if (!agentAddress.equals(other.agentAddress)) {
		    return false;
	    }
	    
	    return true;
    }
	
}
