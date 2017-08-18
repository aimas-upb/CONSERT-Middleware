package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxQueryHandlerSpec")
public class QueryHandlerSpecification extends AgentSpecification {
	private boolean isPrimary;
	
	
	public QueryHandlerSpecification() {}
	
	public QueryHandlerSpecification(AgentAddressConfig agentAddress, AgentPolicy controlPolicy, 
			boolean isPrimary, AgentAddressConfig assignedOrgMgrAddress) {
		super(agentAddress, controlPolicy, assignedOrgMgrAddress);
		
		this.isPrimary = isPrimary;
    }

	public boolean isPrimary() {
		return isPrimary;
	}
}
