package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class AgentSpecification {
	protected AgentAddress agentAddress;
	protected AgentAddress assignedManagerAddress;
	protected AgentPolicy controlPolicy;
	protected AgentType agentType;
	
	public AgentSpecification(AgentAddress agentAddress, AgentType agentType, AgentPolicy controlPolicy, 
			AgentAddress assignedManagerAddress) {
	    this.agentAddress = agentAddress;
	    this.agentType = agentType;
	    this.controlPolicy = controlPolicy;
	    this.assignedManagerAddress = assignedManagerAddress;
    }
	
	public AgentAddress getAgentAddress() {
		return agentAddress;
	}
	
	public String getAgentLocalName() {
		return agentAddress.getLocalName();
	}
	
	public String getAgentName() {
		return agentAddress.getAID().getName();
	}
	
	public AgentPolicy getControlPolicy() {
		return controlPolicy;
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
	
	public static AgentAddress getAddressFromConfig(OntModel cmmConfigModel, Resource agentSpec) {
		AgentAddress agentAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
				agentSpec.getPropertyResourceValue(OrgConf.hasAgentAddress));
		
		return agentAddress;
	}
	
	
	public static AgentPolicy getPolicyFromConfig(OntModel cmmConfigModel, Resource agentSpec) {
		AgentPolicy controlPolicy = AgentPolicy.fromConfigurationModel(cmmConfigModel, 
				agentSpec.getPropertyResourceValue(OrgConf.hasControlPolicy));
		
		return controlPolicy;
	}
	
	public AgentType getType() {
		return agentType;
	}
	
	@Override
    public int hashCode() {
	    return agentAddress.getAID().hashCode();
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
