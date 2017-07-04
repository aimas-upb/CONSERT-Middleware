package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("ann=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#")
@RDFBean("ann:ContextAnnotation")
public class ContextAnnotation {

	private String id;
	
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
