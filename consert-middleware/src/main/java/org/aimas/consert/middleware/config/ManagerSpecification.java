package org.aimas.consert.middleware.config;

import java.net.URI;
import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:OrgMgrSpec")
public class ManagerSpecification extends AgentSpecification {
	
	private AgentAddressConfig parentManager;
	private AgentAddressConfig rootManager;
	private List<AgentAddressConfig> knownRootManagers;
	
	private URI managerType;
	
	
	public ManagerSpecification() {}
	
	public ManagerSpecification(AgentAddressConfig agentAddress, URI managerType, AgentAddressConfig parentManager) {
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
	public AgentAddressConfig getParentManagerAddress() {
		return parentManager;
	}
	
	public void setParentManagerAddress(AgentAddressConfig parentManager) {
		this.parentManager = parentManager;
	}
	
	@RDF("orgconf:hasManagerRoot")
	public AgentAddressConfig getRootManagerAddress() {
		return rootManager;
	}
	
	public void setRootManagerAddress(AgentAddressConfig rootManager) {
		this.rootManager = rootManager;
	}
	
	@RDF("orgconf:knowsManagerRoot")
	public List<AgentAddressConfig> getKnownRootManagers() {
		return knownRootManagers;
	}
	
	public void setKnownRootManagers(List<AgentAddressConfig> knownRootManagers) {
		this.knownRootManagers = knownRootManagers;
	}
}
