package org.aimas.consert.middleware.config;

import java.net.URI;
import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the configuration of an OrgMgr agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:OrgMgrSpec")
public class ManagerSpecification extends AgentSpecification {
	
	private AgentAddress parentManager;
	private AgentAddress rootManager;
	private List<AgentAddress> knownRootManagers;
	
	private URI managerType;
	
	
	public ManagerSpecification() {}
	
	public ManagerSpecification(AgentAddress agentAddress, URI managerType, AgentAddress parentManager) {
	    super(agentAddress, null, null);
	    this.parentManager = parentManager;
	    this.managerType = managerType;
    }
	
	@RDF("orgconf:hasManagerType")
	public URI getManagerType() {
		return managerType;
	}
	
	public void setManagerType(URI managerType) {
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
