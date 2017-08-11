package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ContextDomainSpecification {
	private Resource domainDimension;
	private Resource domainRangeEntity;
	private Resource domainRangeValue;
	
	private Property domainHierarchyProperty;
	private String domainHierarchyDocument;
	
	private ContextModelDefinition domainModelDefinition;

	public ContextDomainSpecification(Resource domainDimension, Resource domainEntity,
            Resource domainValue, ContextModelDefinition domainModelDefinition) {
	    this.domainDimension = domainDimension;
	    this.domainRangeEntity = domainEntity;
	    this.domainRangeValue = domainValue;
	    this.domainModelDefinition = domainModelDefinition;
    }
	
	public ContextDomainSpecification(ContextModelDefinition domainModelDefinition) {
	    this(null, null, null, domainModelDefinition);
    }

	public Resource getDomainDimension() {
		return domainDimension;
	}
	
	public boolean hasDomainDimension() {
		return domainDimension != null;
	}

	public Resource getDomainEntity() {
		return domainRangeEntity;
	}
	
	public boolean hasDomainEntity() {
		return domainRangeEntity != null;
	}

	public Resource getDomainValue() {
		return domainRangeValue;
	}
	
	public boolean hasDomainValue() {
		return domainRangeValue != null;
	}
	
	public Property getDomainHierarchyProperty() {
		return domainHierarchyProperty;
	}
	
	public void setDomainHierarchyProperty(Property domainHierarchyProperty) {
		this.domainHierarchyProperty = domainHierarchyProperty;
	}
	
	public boolean hasDomainHierarchy() {
		return domainHierarchyProperty != null && domainHierarchyDocument != null;
	}
	
	public String getDomainHierarchyDocument() {
		return domainHierarchyDocument;
	}

	public void setDomainHierarchyDocument(String domainHierarchyDocument) {
		this.domainHierarchyDocument = domainHierarchyDocument;
	}
	
	public boolean hasContextModelDefinition() {
		return domainModelDefinition != null;
	}
	
	public ContextModelDefinition getDomainContextModelDefinition() {
		return domainModelDefinition;
	}
	
	
	public static ContextDomainSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource contextDomainResource) {
		Statement domainDimensionStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainDimension);
		Statement domainRangeEntityStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainRangeEntity);
		Statement domainRangeValueStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainRangeValue);
		
		Resource domainDimension = domainDimensionStmt != null ? domainDimensionStmt.getResource() : null;
		Resource domainRangeEntity = domainRangeEntityStmt != null ? domainRangeEntityStmt.getResource() : null;
		Resource domainRangeValue = domainRangeValueStmt != null ? domainRangeValueStmt.getResource() : null;
		
		Statement domainHierarchyPropertyStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainHierarchyProperty);
		Property domainHierarchyProperty = domainHierarchyPropertyStmt != null ? domainHierarchyPropertyStmt.getResource().as(Property.class) : null;
		
		Statement domainHierarchyDocumentStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasDomainHierarchyDocument);
		String domainHierarchyDocument = domainHierarchyDocumentStmt != null ? getFileOrURI(domainHierarchyDocumentStmt.getResource()) : null;
		
		Statement domainModelStmt = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasContextModel);
		ContextModelDefinition contextModelDefinition = null;
		if (domainModelStmt != null) {
			Resource domainModelRes = cmmConfigModel.getProperty(contextDomainResource, OrgConf.hasContextModel).getResource();
			contextModelDefinition = ContextModelDefinition.fromConfigurationModel(cmmConfigModel, domainModelRes);
		}
		
		ContextDomainSpecification contextDomainSpecification = new ContextDomainSpecification(domainDimension, domainRangeEntity, domainRangeValue, 
				contextModelDefinition);
		contextDomainSpecification.setDomainHierarchyProperty(domainHierarchyProperty);
		contextDomainSpecification.setDomainHierarchyDocument(domainHierarchyDocument);
		
		return contextDomainSpecification;
	}
	
	private static String getFileOrURI(Resource documentRes) {
		Statement docFileStmt = documentRes.getProperty(OrgConf.documentPath);
		if (docFileStmt != null) {
			return docFileStmt.getString();
		}
		else {
			docFileStmt = documentRes.getProperty(OrgConf.documentURI);
			return docFileStmt.getString();
		}
	}
}
