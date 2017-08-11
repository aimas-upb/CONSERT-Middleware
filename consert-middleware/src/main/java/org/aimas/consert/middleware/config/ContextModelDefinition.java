package org.aimas.consert.middleware.config;

import java.util.Dictionary;
import java.util.Hashtable;

import org.aimas.ami.cmm.vocabulary.OrgConf;
import org.aimas.ami.contextrep.utils.ContextModelLoader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ContextModelDefinition {
	
	private String modelDocumentManagerFileOrURI;
	private String modelCoreFileOrURI;
	private String modelAnnotationsFileOrURI;
	private String modelConstraintsFileOrURI;
	private String modelFunctionsFileOrURI;
	private String modelRulesFileOrURI;
	
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

	public String getModelDocumentManagerFileOrURI() {
		return modelDocumentManagerFileOrURI;
	}

	public String getModelCoreFileOrURI() {
		return modelCoreFileOrURI;
	}

	public String getModelAnnotationsFileOrURI() {
		return modelAnnotationsFileOrURI;
	}

	public String getModelConstraintsFileOrURI() {
		return modelConstraintsFileOrURI;
	}

	public String getModelFunctionsFileOrURI() {
		return modelFunctionsFileOrURI;
	}

	public String getModelRulesFileOrURI() {
		return modelRulesFileOrURI;
	}
	
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
	
	public static ContextModelDefinition fromConfigurationModel(OntModel cmmConfigModel, Resource contextModelResource) {
		Statement modelDocumentManagerStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelDocumentManager);
		Statement modelCoreStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelCoreDocument);
		Statement modelAnnotationStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelAnnotationsDocument);
		Statement modelConstraintsStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelConstraintsDocument);
		Statement modelFunctionsStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelFunctionsDocument);
		Statement modelRulesStmt = cmmConfigModel.getProperty(contextModelResource, OrgConf.hasModelRulesDocument);
		
		String modelDocumentManagerFileOrURI = 
				modelDocumentManagerStmt != null ? getFileOrURI(modelDocumentManagerStmt.getResource()) : null;
		
		String modelCoreFileOrURI = 
				modelCoreStmt != null ? getFileOrURI(modelCoreStmt.getResource()) : null;
		
		String modelAnnotationsFileOrURI = 
				modelAnnotationStmt != null ? getFileOrURI(modelAnnotationStmt.getResource()) : null;
		
		String modelConstraintsFileOrURI = 
				modelConstraintsStmt != null ? getFileOrURI(modelConstraintsStmt.getResource()) : null;
		
		String modelFunctionsFileOrURI = 
				modelFunctionsStmt != null ? getFileOrURI(modelFunctionsStmt.getResource()) : null;
		
		String modelRulesFileOrURI = 
				modelRulesStmt != null ? getFileOrURI(modelRulesStmt.getResource()) : null;
		
		return new ContextModelDefinition(modelDocumentManagerFileOrURI, modelCoreFileOrURI, 
				modelAnnotationsFileOrURI, modelConstraintsFileOrURI, modelFunctionsFileOrURI, modelRulesFileOrURI);
	}
	
	private static String getFileOrURI(Resource modelDocumentRes) {
		Statement docFileStmt = modelDocumentRes.getProperty(OrgConf.documentPath);
		if (docFileStmt != null) {
			return docFileStmt.getString();
		}
		else {
			docFileStmt = modelDocumentRes.getProperty(OrgConf.documentURI);
			return docFileStmt.getString();
		}
	}
}
