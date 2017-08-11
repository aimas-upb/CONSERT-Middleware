package org.aimas.consert.middleware.config;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SensorSpecification extends AgentSpecification {
	private List<SensingPolicy> sensingPolicies;
	
	public SensorSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, 
			AgentAddress assignedOrgMgrAddress, List<SensingPolicy> sensingPolicies) {
	    
		super(agentAddress, AgentType.CTX_SENSOR, controlPolicy, assignedOrgMgrAddress);
	    this.sensingPolicies = sensingPolicies;
    }

	
	public List<SensingPolicy> getSensingPolicies() {
		return sensingPolicies;
	}
	
	public static SensorSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource sensorSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, sensorSpec);
		
		StmtIterator sensingPolicyIt = sensorSpec.listProperties(OrgConf.hasSensingPolicy);
		List<SensingPolicy> sensingPolicies = new LinkedList<SensingPolicy>();
		
		for (;sensingPolicyIt.hasNext();) {
			SensingPolicy sensingPolicy = SensingPolicy.fromConfigurationModel(cmmConfigModel, 
					sensingPolicyIt.next().getResource());
			sensingPolicies.add(sensingPolicy);
		}
		
		AgentAddress assignedOrgMgrAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
			sensorSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));	
		
		return new SensorSpecification(agentAddress, null, assignedOrgMgrAddress, sensingPolicies);
	}
}
