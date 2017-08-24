package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines the type of an OrgMgr agent according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:OrgMgrType")
public class ManagerType {

	public final static String ROOT = "Root";
	public final static String NODE = "Node";
	public final static String CENTRAL = "Central";
	public final static String MOBILE = "Mobile";
	
	private String managerType;
	
	
	public ManagerType() {}
	
	public ManagerType(String managerType) {
		this.managerType = managerType;
	}

	@RDF("orgmgr:hasManagerType")
	public String getManagerType() {
		return managerType;
	}

	public void setManagerType(String managerType) {
		this.managerType = managerType;
	}
}
