package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines configuration of a CtxCoord agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxCoordSpec")
public class CoordinatorSpecification extends AgentSpecification {
	
	public CoordinatorSpecification() {}
	
	public CoordinatorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress assignedManagerAddress) {
	    
		super(agentAddress, controlPolicy, assignedManagerAddress);
    }
}
