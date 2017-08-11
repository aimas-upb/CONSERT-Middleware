package org.aimas.consert.middleware.config;

import jade.core.AID;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class AgentAddress {
	private String agentLocalName;
	private String agentMTPHost;
	private int agentMTPPort;
	private String agentAppIdentifier;
	
	private CMMAgentContainer agentContainer;
	private AID agentID;
	
	
	public AgentAddress(String agentLocalName, CMMAgentContainer agentContainer, String agentAppIdentifier) {
	    this.agentLocalName = agentLocalName;
	    this.agentMTPHost = agentContainer.getMTPHost();
	    this.agentMTPPort = agentContainer.getMTPPort();
	    
	    this.agentContainer = agentContainer;
	    this.agentAppIdentifier = agentAppIdentifier;
	}
	
	public String getLocalName() {
		return agentLocalName + "__" + agentAppIdentifier;
	}
	
	public String getMTPHost() {
		return agentMTPHost;
	}
	
	public int getMTPPort() {
		return agentMTPPort;
	}
	
	public CMMAgentContainer getAgentContainer() {
	    return agentContainer;
    }
	
	public AID getAID() {
		if (agentID == null) {
			// If we don't have an agent container, we assume the local name suffices 
			// to construct the correct global AID
			if (agentContainer == null) {
				AID aid = new AID(getLocalName(), false);
				aid.addAddresses(getMTPAddress());
				
				agentID = aid;
			}
			
			// Otherwise, use the platform name from the Container to construct the global AID
			String globalAgentName = getLocalName() + "@" + agentContainer.getPlatformName();
			AID aid = new AID(globalAgentName, true);
			aid.addAddresses(getMTPAddress());
			
			agentID = aid;
		}
		
		return agentID;
	}
	
	public String getMTPAddress() {
		return "http://" + agentMTPHost + ":" + agentMTPPort + "/acc";
	}
	
	
	public static AgentAddress fromConfigurationModel(OntModel cmmConfigurationModel, Resource agentAddressResource) {
		if (agentAddressResource == null)
			return null;
		
		String agentLocalName = agentAddressResource.getProperty(OrgConf.agentName).getString();
		CMMAgentContainer agentContainer = CMMAgentContainer.fromConfigurationModel(cmmConfigurationModel, 
				agentAddressResource.getPropertyResourceValue(OrgConf.agentContainer));
		String agentAppIdentifier = agentAddressResource.getProperty(OrgConf.agentAppIdentifier).getString();
		
		return new AgentAddress(agentLocalName, agentContainer, agentAppIdentifier);
	}

	@Override
    public int hashCode() {
	    return getAID().hashCode();
    }
	
	
	@Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    
	    if (obj == null) {
		    return false;
	    }
	    
	    if (!(obj instanceof AgentAddress)) {
		    return false;
	    }
	    
	    AgentAddress other = (AgentAddress) obj;
	    if (!getAID().equals(other.getAID())) {
		    return false;
	    }
	    
	    return true;
    }
}
