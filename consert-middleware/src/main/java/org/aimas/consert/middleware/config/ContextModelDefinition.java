package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines a context model according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:ContextModelDefinition")
public class ContextModelDefinition {
	
	private String modelDocumentManagerFileOrURI;
	private String modelCoreFileOrURI;
	private String modelAnnotationsFileOrURI;
	private String modelConstraintsFileOrURI;
	private String modelFunctionsFileOrURI;
	private String modelRulesFileOrURI;
	
	
	public ContextModelDefinition() {}
	
	public ContextModelDefinition(String modelDocumentManagerFileOrURI,
            String modelCoreFileOrURI, String modelAnnotationsFileOrURI,
            String modelConstraintsFileOrURI, String modelFunctionsFileOrURI,
            String modelRulesFileOrURI) {
	    
		this.modelDocumentManagerFileOrURI = modelDocumentManagerFileOrURI;
	    this.modelCoreFileOrURI = modelCoreFileOrURI;
	    this.modelAnnotationsFileOrURI = modelAnnotationsFileOrURI;
	    this.modelConstraintsFileOrURI = modelConstraintsFileOrURI;
	    this.modelFunctionsFileOrURI = modelFunctionsFileOrURI;
	    this.modelRulesFileOrURI = modelRulesFileOrURI;
    }

	@RDF("orgconf:hasModelDocumentManager")
	public String getModelDocumentManagerFileOrURI() {
		return modelDocumentManagerFileOrURI;
	}
	
	public void setmodelDocumentManagerFileOrURI(String modelDocumentManagerFileOrURI) {
		this.modelDocumentManagerFileOrURI = modelDocumentManagerFileOrURI;
	}

	@RDF("orgconf:hasModelCoreDocument")
	public String getModelCoreFileOrURI() {
		return modelCoreFileOrURI;
	}
	
	public void setModelCoreFileOrURI(String modelCoreFileOrURI) {
		this.modelCoreFileOrURI = modelCoreFileOrURI;
	}

	@RDF("orgconf:hasModelAnnotationsDocument")
	public String getModelAnnotationsFileOrURI() {
		return modelAnnotationsFileOrURI;
	}
	
	public void setModelAnnotationsFileOrURI(String modelAnnotationsFileOrURI) {
		this.modelAnnotationsFileOrURI = modelAnnotationsFileOrURI;
	}

	@RDF("orgconf:hasModelContraintsDocument")
	public String getModelConstraintsFileOrURI() {
		return modelConstraintsFileOrURI;
	}
	
	public void setModelContraintsFileOrURI(String modelConstraintsFileOrURI) {
		this.modelConstraintsFileOrURI = modelConstraintsFileOrURI;
	}
 
	@RDF("orgconf:hasModelFunctionsDocument")
	public String getModelFunctionsFileOrURI() {
		return modelFunctionsFileOrURI;
	}
	
	public void setModelFunctionsFileOrURI(String modelFunctionsFileOrURI) {
		this.modelFunctionsFileOrURI = modelFunctionsFileOrURI;
	}

	@RDF("orgconf:hasModelRulesDocument")
	public String getModelRulesFileOrURI() {
		return modelRulesFileOrURI;
	}
	
	public void setModelRulesFileOrURI(String modelRulesFileOrURI) {
		this.modelRulesFileOrURI = modelRulesFileOrURI;
	}
	
	/*
	public Dictionary<String, String> getContextModelFileDictionary() {
		Dictionary<String, String> contextModelFileProps = new Hashtable<String, String>();
		
		if (modelDocumentManagerFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_DOCMGR_KEY, modelDocumentManagerFileOrURI);
	    
	    if (modelCoreFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_CORE_URI_KEY, modelCoreFileOrURI);
	    
	    if (modelAnnotationsFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_ANNOTATION_URI_KEY, modelAnnotationsFileOrURI);
	    
	    if (modelConstraintsFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_CONSTRAINT_URI_KEY, modelConstraintsFileOrURI);
	    
	    if (modelFunctionsFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_FUNCTIONS_URI_KEY, modelFunctionsFileOrURI);
	    
	    if (modelRulesFileOrURI != null)
	    	contextModelFileProps.put(ContextModelLoader.DOMAIN_ONT_RULES_URI_KEY, modelRulesFileOrURI);
	
	    return contextModelFileProps;
	}
	*/
}
