package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("core=http://pervasive.semanticweb.org/ont/2014/05/consert/core#")
@RDFBean("core:ContextAssertion")
public class ContextAssertion extends RDFObject {

	private ContextEntity assertionRole;
	
	
	@RDF("core:assertionRole")
	public ContextEntity getAssertionRole() {
		return assertionRole;
	}
	
	public void setAssertionRole(ContextEntity assertionRole) {
		this.assertionRole = assertionRole;
	}
}
