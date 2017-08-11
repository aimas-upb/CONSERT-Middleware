package org.aimas.consert.middleware.config;

import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:OrgMgrType")
public class ManagerSpecification extends AgentSpecification {
	
	private AgentAddress parentManager;
	private AgentAddress rootManager;
	private List<AgentAddress> knownRootManagers;
	
	private ManagerType managerType;
	
	
	public ManagerSpecification() {}
	
	public ManagerSpecification(AgentAddress agentAddress, ManagerType managerType, AgentAddress parentManager) {
	    super(agentAddress, null, null);
	    this.parentManager = parentManager;
	    this.managerType = managerType;
    }
	
	@RDF("orgconf:hasManagerType")
	public ManagerType getManagerType() {
		return managerType;
	}
	
	public void setManagerType(ManagerType managerType) {
		this.managerType = managerType;
	}
	
	@RDF("orgconf:hasManagerParent")
	public AgentAddress getParentManagerAddress() {
		return parentManager;
	}
	
	public void setParentManagerAddress(AgentAddress parentManager) {
		this.parentManager = parentManager;
	}
	
	@RDF("orgconf:hasManagerRoot")
	public AgentAddress getRootManagerAddress() {
		return rootManager;
	}
	
	public void setRootManagerAddress(AgentAddress rootManager) {
		this.rootManager = rootManager;
	}
	
	@RDF("orgconf:knowsManagerRoot")
	public List<AgentAddress> getKnownRootManagers() {
		return knownRootManagers;
	}
	
	public void setKnownRootManagers(List<AgentAddress> knownRootManagers) {
		this.knownRootManagers = knownRootManagers;
	}
}
