package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("annotation=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#")
@RDFBean("annotation:BasicAnnotation")
public class BasicAnnotation implements ContextAnnotation {

	private Resource unstructuredValue;
	private String id;
	
	
	public BasicAnnotation() {
		this.id = UUID.randomUUID().toString();
	}


	@RDF("annotation:hasUnstructuredValue")
	public Resource getUnstructuredValue() {
		return unstructuredValue;
	}

	@RDF("annotation:hasUnstructuredValue")
	public void setUnstructuredValue(Resource unstructuredValue) {
		this.unstructuredValue = unstructuredValue;
	}
	
	@RDFSubject(prefix="annotation:BasicAnnotation/")
	public String getId() {
		return this.id;
	}
}
