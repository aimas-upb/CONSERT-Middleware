package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the configuration of a platform according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:PlatformSpec")
public class PlatformSpecification {
	
	private CMMAgentContainer platformAgentContainer;
	
	
	public PlatformSpecification() {}
	
	public PlatformSpecification(CMMAgentContainer platformAgentContainer) {
	    this.platformAgentContainer = platformAgentContainer;
    }
	
	@RDF("orgconf:hasAgentContainer")
	public CMMAgentContainer getPlatformAgentContainer() {
		return platformAgentContainer;
	}
	
	public void setPlatformAgentContainer(CMMAgentContainer platformAgentContainer) {
		this.platformAgentContainer = platformAgentContainer;
	}
}
