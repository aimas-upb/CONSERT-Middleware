package org.aimas.consert.middleware.config;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManagerSpecification extends AgentSpecification {
	public static enum ManagerType {
		Root, Node, Central, Mobile;
		
		public static ManagerType getFromResource(Resource typeResource) {
			if (typeResource.equals(OrgConf.Root)) {
				return Root;
			}
			else if (typeResource.equals(OrgConf.Node)) {
				return Node;
			}
			else if (typeResource.equals(OrgConf.Mobile)) {
				return Mobile;
			}
			
			return Central;
		}
	}
	
	private AgentAddress parentManager;
	private AgentAddress rootManager;
	private List<AgentAddress> knownRootManagers;
	
	private ManagerType managerType;
	
	public ManagerSpecification(AgentAddress agentAddress, ManagerType managerType, AgentAddress parentManager) {
	    super(agentAddress, AgentType.ORG_MGR, null, null);
	    this.parentManager = parentManager;
	    this.managerType = managerType;
    }
	
	public ManagerType getManagerType() {
		return managerType;
	}
	
	public AgentAddress getParentManagerAddress() {
		return parentManager;
	}
	
	public AgentAddress getRootManagerAddress() {
		return rootManager;
	}
	
	public void setRootManagerAddress(AgentAddress rootManager) {
		this.rootManager = rootManager;
	}
	
	public List<AgentAddress> getKnownRootManagers() {
		return knownRootManagers;
	}
	
	public void setKnownRootManagers(List<AgentAddress> knownRootManagers) {
		this.knownRootManagers = knownRootManagers;
	}
	
	
	public static ManagerSpecification fromConfigurationModel(OntModel cmmConfigModel) {
		// There is only one OrgMgr specification in a configuration
		Resource orgMgrSpec = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.OrgMgrSpec).next();
		
		if (orgMgrSpec == null) {
			return null;
		}
		
		// required
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, orgMgrSpec);
		
		ManagerType managerType = ManagerType.getFromResource(orgMgrSpec.getPropertyResourceValue(OrgConf.hasManagerType));
		AgentAddress parentMgrAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
				orgMgrSpec.getPropertyResourceValue(OrgConf.hasManagerParent));
		
		// optional
		AgentAddress rootMgrAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
				orgMgrSpec.getPropertyResourceValue(OrgConf.hasManagerRoot));
		
		List<AgentAddress> knownRootManagers = getKnownRootManagers(cmmConfigModel, orgMgrSpec);
		
		// create the manager specification
		ManagerSpecification managerSpec = new ManagerSpecification(agentAddress, managerType, parentMgrAddress);
		if (rootMgrAddress != null) 
			managerSpec.setRootManagerAddress(rootMgrAddress);
		
		if (!knownRootManagers.isEmpty()) 
			managerSpec.setKnownRootManagers(knownRootManagers);
		
		return managerSpec;
	}
	
	
	private static List<AgentAddress> getKnownRootManagers(OntModel cmmConfigModel, Resource orgMgrSpec) {
		List<AgentAddress> knownRootManagers = new LinkedList<AgentAddress>();
		StmtIterator knownMgrIt = orgMgrSpec.listProperties(OrgConf.knowsManagerRoot);
		
		for (;knownMgrIt.hasNext();) {
			knownRootManagers.add(AgentAddress.fromConfigurationModel(cmmConfigModel, knownMgrIt.next().getResource()));
		}
		
		return knownRootManagers;
	}
}
