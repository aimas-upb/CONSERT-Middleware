package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:DeploymentType")
public class DeploymentType {
		
	public static String CENTRALIZED_LOCAL = "CentralizedLocal";
	public static String DECENTRALIZED_HIERARCHICAL = "DecentralizedHierarchical";
	
	private String deploymentType;
	
	
	public DeploymentType() {}
	
	public DeploymentType(String deploymentType) {
		this.deploymentType = deploymentType;
	}
	
	@RDF("orgconf:deploymentType")
	public String getDeploymentType() {
		return deploymentType;
	}
	
	public void setDeploymentType(String deploymentType) {
		this.deploymentType = deploymentType;
	}
}
