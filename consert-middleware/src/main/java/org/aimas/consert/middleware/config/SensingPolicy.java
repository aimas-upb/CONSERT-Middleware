package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class SensingPolicy extends AgentPolicy {
	
	private Resource contextAssertionRes;
	private String assertionAdaptorClass;
	
	public SensingPolicy(String fileNameOrURI, Resource contextAssertion, String assertionAdaptorClass) {
		super(fileNameOrURI);
		
		this.contextAssertionRes = contextAssertion;
		this.assertionAdaptorClass = assertionAdaptorClass;
	}

	/**
	 * @return The {@link Resource} defining the ContextAssertion for which this SensorPolicy is set
	 */
    public Resource getContextAssertionRes() {
	    return contextAssertionRes;
    }

	/**
	 * @return The implementation class for the SensorAdaptor service which interfaces with the underlying
	 * sensor management middleware in order to generate ContextAssertion data and handle TASKING commands.
	 */
    public String getAssertionAdaptorClass() {
	    return assertionAdaptorClass;
    }
    
    
    public static SensingPolicy fromConfigurationModel(OntModel cmmConfigModel, Resource policyResource) {
    	if (policyResource == null)
    		return null;
    	
    	String documentSource = AgentPolicy.getDocumentSource(cmmConfigModel, policyResource);
    	
    	Resource contextAssertionRes = policyResource.getPropertyResourceValue(OrgConf.forContextAssertion);
    	String assertionAdaptorClass = getAdaptorClass(cmmConfigModel, 
    			policyResource.getPropertyResourceValue(OrgConf.usesAssertionAdaptor));
    	
    	return new SensingPolicy(documentSource, contextAssertionRes, assertionAdaptorClass);
    }
    
    
    private static String getAdaptorClass(OntModel cmmConfigModel, Resource assertionAdaptorRes) {
    	return assertionAdaptorRes.getProperty(OrgConf.hasQualifiedName).getString();
    }
}
