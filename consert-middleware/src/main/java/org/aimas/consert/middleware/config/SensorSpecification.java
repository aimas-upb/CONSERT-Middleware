package org.aimas.consert.middleware.config;

import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxSensorSpec")
public class SensorSpecification extends AgentSpecification {
	
	private List<SensingPolicy> sensingPolicies;
	
	
	public SensorSpecification() {}
	
	public SensorSpecification(AgentAddressConfig agentAddress, AgentPolicy controlPolicy, 
			AgentAddressConfig assignedOrgMgrAddress, List<SensingPolicy> sensingPolicies) {
	    
		super(agentAddress, controlPolicy, assignedOrgMgrAddress);
	    this.sensingPolicies = sensingPolicies;
    }

	@RDF("orgconf:hasSensingPolicy")
	public List<SensingPolicy> getSensingPolicies() {
		return sensingPolicies;
	}
	
	public void setSensingPolicies(List<SensingPolicy> sensingPolicies) {
		this.sensingPolicies = sensingPolicies;
	}
}
