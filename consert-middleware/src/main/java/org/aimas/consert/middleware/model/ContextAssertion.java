package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("core=http://pervasive.semanticweb.org/ont/2014/05/consert/core#")
@RDFBean("core:ContextAssertion")
public class ContextAssertion {

	private String id;
	

	@RDFSubject
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
