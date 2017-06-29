package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("core=http://pervasive.semanticweb.org/ont/2014/05/consert/core#")
@RDFBean("core:ContextAssertion")
public class ContextAssertion {

	private ContextEntity assertionRole;
	private String id;
	
	
	@RDF("core:assertionRole")
	public ContextEntity getAssertionRole() {
		return assertionRole;
	}
	
	@RDF("core:assertionRole")
	public void setAssertionRole(ContextEntity assertionRole) {
		this.assertionRole = assertionRole;
	}
	
	
	@RDFSubject
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
