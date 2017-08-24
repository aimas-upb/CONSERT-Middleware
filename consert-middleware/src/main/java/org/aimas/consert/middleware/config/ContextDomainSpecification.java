package org.aimas.consert.middleware.config;

import java.net.URI;

import org.aimas.consert.model.content.ContextEntity;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines a context domain according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:ContextDomain")
public class ContextDomainSpecification {
	
	private URI contextDimension;
	private URI domainRangeEntity;
	private ContextEntity domainRangeValue;
	
	private URI domainHierarchyProperty;
	private String domainHierarchyDocument;
	
	private ContextModelDefinition domainModelDefinition;
	
	
	public ContextDomainSpecification() {}

	public ContextDomainSpecification(URI contextDimension, URI domainEntity,
            ContextEntity domainValue, ContextModelDefinition domainModelDefinition) {
	    this.contextDimension = contextDimension;
	    this.domainRangeEntity = domainEntity;
	    this.domainRangeValue = domainValue;
	    this.domainModelDefinition = domainModelDefinition;
    }
	
	public ContextDomainSpecification(ContextModelDefinition domainModelDefinition) {
	    this(null, null, null, domainModelDefinition);
    }

	@RDF("orgconf:hasContextDimension")
	public URI getContextDimension() {
		return contextDimension;
	}
	
	public void setContextDimension(URI contextDimension) {
		this.contextDimension = contextDimension;
	}
	
	public boolean hasDomainDimension() {
		return contextDimension != null;
	}

	@RDF("orgconf:hasDomainRangeEntity")
	public URI getDomainEntity() {
		return domainRangeEntity;
	}
	
	public void setDomainEntity(URI domainRangeEntity) {
		this.domainRangeEntity = domainRangeEntity;
	}
	
	public boolean hasDomainEntity() {
		return domainRangeEntity != null;
	}

	@RDF("orgconf:hasDomainRangeValue")
	public ContextEntity getDomainValue() {
		return domainRangeValue;
	}
	
	public void setDomainValue(ContextEntity domainRangeValue) {
		this.domainRangeValue = domainRangeValue;
	}
	
	public boolean hasDomainValue() {
		return domainRangeValue != null;
	}
	
	@RDF("orgconf:hasDomainhierarchyProperty")
	public URI getDomainHierarchyProperty() {
		return domainHierarchyProperty;
	}
	
	public void setDomainHierarchyProperty(URI domainHierarchyProperty) {
		this.domainHierarchyProperty = domainHierarchyProperty;
	}
	
	public boolean hasDomainHierarchy() {
		return domainHierarchyProperty != null && domainHierarchyDocument != null;
	}
	
	@RDF("orgconf:hasDomainHierarchyDocument")
	public String getDomainHierarchyDocument() {
		return domainHierarchyDocument;
	}

	public void setDomainHierarchyDocument(String domainHierarchyDocument) {
		this.domainHierarchyDocument = domainHierarchyDocument;
	}
	
	public boolean hasContextModelDefinition() {
		return domainModelDefinition != null;
	}
	
	@RDF("orgconf:hasContextModel")
	public ContextModelDefinition getDomainContextModelDefinition() {
		return domainModelDefinition;
	}
}
