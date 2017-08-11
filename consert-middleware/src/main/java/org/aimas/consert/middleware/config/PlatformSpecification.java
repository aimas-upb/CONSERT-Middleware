package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


public class PlatformSpecification {
	private CMMAgentContainer platformAgentContainer;
	
	public PlatformSpecification(CMMAgentContainer platformAgentContainer) {
	    this.platformAgentContainer = platformAgentContainer;
    }
	
	public CMMAgentContainer getPlatformAgentContainer() {
		return platformAgentContainer;
	}
	
	public static PlatformSpecification fromConfigurationModel(OntModel platformConfigModel) {
		Resource platformSpec = platformConfigModel.listResourcesWithProperty(RDF.type, OrgConf.PlatformSpec).next();
		
		CMMAgentContainer platformAgentContainer = CMMAgentContainer.fromConfigurationModel(platformConfigModel, 
				platformSpec.getPropertyResourceValue(OrgConf.hasAgentContainer));
		
		return new PlatformSpecification(platformAgentContainer);
	}
}
