package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:AgentContainer")
public class CMMAgentContainer {
    
	private String platformName;
	private String containerHost;
	private int containerPort;
	private boolean isMainContainer;
	
	private CMMAgentContainer mainContainer;

	
	public CMMAgentContainer() {}
	
	public CMMAgentContainer(boolean isMainContainer, String containerHost, int containerPort, 
			String platformName, CMMAgentContainer mainContainer) {
	    
		this.isMainContainer = isMainContainer;
	    this.containerHost = containerHost;
	    this.containerPort = containerPort;
	    this.platformName = platformName;
	    this.mainContainer = mainContainer;
    }

	@RDF("orgconf:isMainContainer")
	public boolean isMainContainer() {
		return isMainContainer;
	}
	
	public void setIsMainContainer(boolean isMainContainer) {
		this.isMainContainer = isMainContainer;
	}

	@RDF("orgconf:containerHost")
	public String getContainerHost() {
		return containerHost;
	}
	
	public void setContainerHost(String containerHost) {
		this.containerHost = containerHost;
	}

	@RDF("orgconf:containerPort")
	public int getContainerPort() {
		return containerPort;
	}
	
	public void setContainerPort(int containerPort) {
		this.containerPort = containerPort;
	}
	
	@RDF("orgconf:platformName")
	public String getPlatformName() {
		return platformName;
	}
	
	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}
	
	@RDF("orgconf:hasMainContainer")
	public CMMAgentContainer getMainContainer() {
		return mainContainer;
	}
	
	public void setMainContainer(CMMAgentContainer mainContainer) {
		this.mainContainer = mainContainer;
	}
}
