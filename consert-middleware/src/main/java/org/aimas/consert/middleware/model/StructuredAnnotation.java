package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("annotation=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#")
@RDFBean("annotation:StructuredAnnotation")
public class StructuredAnnotation implements ContextAnnotation {

	private Function continuityFonction;
	private Function joinOp;
	private Function meetOp;
	private String id;
	
	
	public StructuredAnnotation() {
		this.id = UUID.randomUUID().toString();
	}
	
	
	@RDF("annotation:hasContinuityFunction")
	public Function getContinuityFonction() {
		return continuityFonction;
	}
	
	@RDF("annotation:hasContinuityFunction")
	public void setContinuityFonction(Function continuityFonction) {
		this.continuityFonction = continuityFonction;
	}
	
	@RDF("annotation:hasJoinOp")
	public Function getJoinOp() {
		return joinOp;
	}
	
	@RDF("annotation:hasJoinOp")
	public void setJoinOp(Function joinOp) {
		this.joinOp = joinOp;
	}
	
	@RDF("annotation:hasMeetOp")
	public Function getMeetOp() {
		return meetOp;
	}
	
	@RDF("annotation:hasMeetOp")
	public void setMeetOp(Function meetOp) {
		this.meetOp = meetOp;
	}
	
	@RDFSubject(prefix="annotation:StructuredAnnotation/")
	public String getId() {
		return this.id;
	}
}
