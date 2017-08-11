package org.aimas.consert.middleware.config;

import java.io.Serializable;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class CMMAgentContainer {
    
	private String platformName;
	private String containerHost;
	private int containerPort;
	private boolean isMainContainer;
	private String mtpHost;
	private int mtpPort;
	
	private CMMAgentContainer mainContainer;

	public CMMAgentContainer(boolean isMainContainer, String containerHost, int containerPort, 
			String platformName, String mtpHost, int mtpPort, CMMAgentContainer mainContainer) {
	    
		this.isMainContainer = isMainContainer;
	    this.containerHost = containerHost;
	    this.containerPort = containerPort;
	    this.platformName = platformName;
	    this.mtpHost = mtpHost;
	    this.mtpPort = mtpPort;
	    this.mainContainer = mainContainer;
    }

	public boolean isMainContainer() {
		return isMainContainer;
	}

	public String getContainerHost() {
		return containerHost;
	}

	public int getContainerPort() {
		return containerPort;
	}
	
	public String getMTPHost() {
		return mtpHost;
	}

	public int getMTPPort() {
		return mtpPort;
	}
	
	public String getPlatformName() {
		return platformName;
	}
	
	public CMMAgentContainer getMainContainer() {
		return mainContainer;
	}
	
	
	public static CMMAgentContainer fromConfigurationModel(OntModel cmmConfigurationModel, Resource containerRes) {
		if (containerRes == null) 
			return null;
		
		String containerHost = containerRes.getProperty(OrgConf.containerHost).getString();
		int containerPort = containerRes.getProperty(OrgConf.containerPort).getInt();
		
		String mtpHost = containerRes.getProperty(OrgConf.hasMTPHost).getString();
		int mtpPort = containerRes.getProperty(OrgConf.hasMTPPort).getInt();
		
		
		boolean isMainContainer = containerRes.getProperty(OrgConf.isMainContainer).getBoolean();
		String platformName = containerRes.getProperty(OrgConf.platformName).getString();
		
		Statement mainContainerStmt = containerRes.getProperty(OrgConf.hasMainContainer);
		if (mainContainerStmt != null) {
			Resource mainContainerRes = mainContainerStmt.getResource();
			CMMAgentContainer mainContainer = CMMAgentContainer.fromConfigurationModel(cmmConfigurationModel, mainContainerRes);
			return new CMMAgentContainer(isMainContainer, containerHost, containerPort, platformName, mtpHost, mtpPort, mainContainer);
		}
		
		return new CMMAgentContainer(isMainContainer, containerHost, containerPort, platformName, mtpHost, mtpPort, null);
	}
}
