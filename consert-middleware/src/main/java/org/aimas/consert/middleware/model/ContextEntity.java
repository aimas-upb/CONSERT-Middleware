package org.aimas.consert.middleware.model;

import java.net.URI;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("core=http://pervasive.semanticweb.org/ont/2014/05/consert/core#")
@RDFBean("core:ContextEntity")
public class ContextEntity {

	private URI derivedDataAssertion;
	private ContextEntity derivedRelationAssertion;
	private URI entityDataAssertion;
	private URI entityDataDescription;
	private ContextEntity entityRelationAssertion;
	private ContextEntity entityRelationDescription;
	private URI profiledDataAssertion;
	private URI sensedDataAssertion;
	private ContextEntity sensedRelationAssertion;
	private String id;
	
	
	@RDF("core:derivedDataAssertion")
	public URI getDerivedDataAssertion() {
		return derivedDataAssertion;
	}

	@RDF("core:derivedDataAssertion")
	public void setDerivedDataAssertion(URI derivedDataAssertion) {
		this.derivedDataAssertion = derivedDataAssertion;
	}
	
	@RDF("core:derivedRelationAssertion")
	public ContextEntity getDerivedRelationAssertion() {
		return derivedRelationAssertion;
	}

	@RDF("core:derivedRelationAssertion")
	public void setDerivedRelationAssertion(ContextEntity derivedRelationAssertion) {
		this.derivedRelationAssertion = derivedRelationAssertion;
	}

	@RDF("core:entityDataAssertion")
	public URI getEntityDataAssertion() {
		return entityDataAssertion;
	}

	@RDF("core:entityDataAssertion")
	public void setEntityDataAssertion(URI entityDataAssertion) {
		this.entityDataAssertion = entityDataAssertion;
	}

	@RDF("core:entityDataDescription")
	public URI getEntityDataDescription() {
		return entityDataDescription;
	}

	@RDF("core:entityDataDescription")
	public void setEntityDataDescription(URI entityDataDescription) {
		this.entityDataDescription = entityDataDescription;
	}

	@RDF("core:entityRelationAssertion")
	public ContextEntity getEntityRelationAssertion() {
		return entityRelationAssertion;
	}

	@RDF("core:entityRelationAssertion")
	public void setEntityRelationAssertion(ContextEntity entityRelationAssertion) {
		this.entityRelationAssertion = entityRelationAssertion;
	}

	@RDF("core:entityRelationDescription")
	public ContextEntity getEntityRelationDescription() {
		return entityRelationDescription;
	}

	@RDF("core:entityRelationDescription")
	public void setEntityRelationDescription(ContextEntity entityRelationDescription) {
		this.entityRelationDescription = entityRelationDescription;
	}

	@RDF("core:profiledDataAssertion")
	public URI getProfiledDataAssertion() {
		return profiledDataAssertion;
	}

	@RDF("core:profiledDataAssertion")
	public void setProfiledDataAssertion(URI profiledDataAssertion) {
		this.profiledDataAssertion = profiledDataAssertion;
	}

	@RDF("core:sensedDataAssertion")
	public URI getSensedDataAssertion() {
		return sensedDataAssertion;
	}

	@RDF("core:sensedDataAssertion")
	public void setSensedDataAssertion(URI sensedDataAssertion) {
		this.sensedDataAssertion = sensedDataAssertion;
	}

	@RDF("core:sensedRelationAssertion")
	public ContextEntity getSensedRelationAssertion() {
		return sensedRelationAssertion;
	}

	@RDF("core:sensedRelationAssertion")
	public void setSensedRelationAssertion(ContextEntity sensedRelationAssertion) {
		this.sensedRelationAssertion = sensedRelationAssertion;
	}
	
	@RDFSubject
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
