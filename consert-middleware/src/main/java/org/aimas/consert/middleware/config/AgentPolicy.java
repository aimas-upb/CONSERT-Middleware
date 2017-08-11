package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class AgentPolicy {
	/**
	 * The filename or URI of the document that holds the agent specific control policy constructs.
	 */
	private String fileNameOrURI;
	
	
	public AgentPolicy(String fileNameOrURI) {
	    this.fileNameOrURI = fileNameOrURI;
    }
	
	/**
	 * @return The filename or URI of the document that holds the agent specific control policy constructs.
	 */
    public String getFileNameOrURI() {
	    return fileNameOrURI;
    }
	
    
    public static AgentPolicy fromConfigurationModel(OntModel cmmConfigModel, Resource policyResource) {
    	if (policyResource == null)
    		return null;
    	
    	String documentSource = getDocumentSource(cmmConfigModel, policyResource);
    	
    	return new AgentPolicy(documentSource);
    }
    
    protected static String getDocumentSource(OntModel cmmConfigModel, Resource policyResource) {
    	Resource contentDoc = policyResource.getPropertyResourceValue(OrgConf.hasPolicyDocument);
    	
    	Statement path = contentDoc.getProperty(OrgConf.documentPath);
    	Statement uri = contentDoc.getProperty(OrgConf.documentURI);
    	
    	if (path != null) {
    		return path.getString();
    	}
    	
    	return uri.getString();
    }
}
