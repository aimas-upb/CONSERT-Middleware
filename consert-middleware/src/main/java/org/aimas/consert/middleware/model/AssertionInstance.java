package org.aimas.consert.middleware.model;

import java.util.ArrayList;
import java.util.List;

import org.aimas.consert.model.annotations.ContextAnnotation;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFContainer.ContainerType;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:AssertionInstance")
public class AssertionInstance {

	private List<ContextAnnotation> annotations;
	private String id;
	
	
	public AssertionInstance() {
		this.annotations = new ArrayList<ContextAnnotation>();
	}

	
	@RDF("annotation:hasAnnotation")
	@RDFContainer(ContainerType.LIST)
	public List<ContextAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<ContextAnnotation> annotations) {
		this.annotations = annotations;
	}
	
	public void addAnnotation(ContextAnnotation ca) {
		this.annotations.add(ca);
	}
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return this.id + ": " + this.annotations;
	}
}
