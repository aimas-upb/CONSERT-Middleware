package org.aimas.consert.middleware.config;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class UserSpecification extends AgentSpecification {
	private String contextDomainUserDocument;
	
	public UserSpecification(AgentAddress agentAddress, AgentPolicy controlPolicy, AgentAddress assignedManagerAddress, 
			String contextDomainUserDocument) {
		super(agentAddress, AgentType.CTX_USER, controlPolicy, assignedManagerAddress);
		
	    this.assignedManagerAddress = assignedManagerAddress;
	    this.contextDomainUserDocument = contextDomainUserDocument;
	}
	
	public String getContextDomainUserModel() {
		return contextDomainUserDocument;
	}
	
	public boolean hasContextDomainUserModel() {
		return contextDomainUserDocument != null;
	}
	
	public static UserSpecification fromConfigurationModel(OntModel cmmConfigModel, Resource userSpec) {
		AgentAddress agentAddress = AgentSpecification.getAddressFromConfig(cmmConfigModel, userSpec);
		
		AgentAddress assignedManagerAddress = AgentAddress.fromConfigurationModel(cmmConfigModel, 
				userSpec.getPropertyResourceValue(OrgConf.assignedOrgManager));
		
		String contextDomainUserDoc = null;
		Statement st = cmmConfigModel.getProperty(userSpec, OrgConf.hasContextDomainUserDoc);
		if (st != null) {
			contextDomainUserDoc = getFileOrURI(st.getResource());
		}
		
		// We don't have any Control Policy for the CtxUser for now
		return new UserSpecification(agentAddress, null, assignedManagerAddress, contextDomainUserDoc);
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
