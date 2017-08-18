package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:ApplicationSpec")
public class ApplicationSpecification {
	
	private String appIdentifier;
	private DeploymentType appDeploymentType;
	private ContextDomainSpecification localContextDomain;
	
	
	public ApplicationSpecification() {}
	
	public ApplicationSpecification(String appIdentifier, DeploymentType appDeploymentType, ContextDomainSpecification localContextDomain) {
	    this.appIdentifier = appIdentifier;
	    this.appDeploymentType = appDeploymentType;
	    this.localContextDomain = localContextDomain;
    }

	@RDF("orgconf:appIdentificationName")
	public String getAppIdentifier() {
		return appIdentifier;
	}
	
	public void setAppIdentifier(String appIdentifier) {
		this.appIdentifier = appIdentifier;
	}

	@RDF("orgconf:appDeploymentType")
	public DeploymentType getAppDeploymentType() {
		return appDeploymentType;
	}
	
	public void setAppDeploymentType(DeploymentType appDeploymentType) {
		this.appDeploymentType = appDeploymentType;
	}

	@RDF("orgconf:hasContextDomain")
	public ContextDomainSpecification getLocalContextDomain() {
		return localContextDomain;
	}
	
	public void setLocalContextDomain(ContextDomainSpecification localContextDomain) {
		this.localContextDomain = localContextDomain;
	}
}
