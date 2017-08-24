package org.aimas.consert.middleware.config;

import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the configuration of a CtxSensor agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxSensorSpec")
public class SensorSpecification extends AgentSpecification {
	
	private List<SensingPolicy> sensingPolicies;
	
	
	public SensorSpecification() {}
	
	public SensorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress assignedOrgMgrAddress, List<SensingPolicy> sensingPolicies) {
	    
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
