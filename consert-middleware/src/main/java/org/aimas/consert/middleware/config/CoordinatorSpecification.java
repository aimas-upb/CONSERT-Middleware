package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxCoordSpec")
public class CoordinatorSpecification extends AgentSpecification {
	
	public CoordinatorSpecification() {}
	
	public CoordinatorSpecification(AgentAddressConfig agentAddress, AgentPolicy controlPolicy, 
			AgentAddressConfig assignedManagerAddress) {
	    
		super(agentAddress, controlPolicy, assignedManagerAddress);
    }
}
