package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * AssertionUpdateMode from CONSERT provisioning ontology
 */
@RDFNamespaces("provisioning=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/coordconf#")
@RDFBean("provisioning:AssertionUpdateMode")
public class AssertionUpdateMode {
	
	public static final String TIME_BASED = "time-based";
	public static final String CHANGE_BASED = "change-based";

	private String updateMode;  // time-based or change-based
	private int updateRate;  // for time-based update mode only
	
	public AssertionUpdateMode() {}

	@RDF("provisioning:hasMode")
	public String getUpdateMode() {
		return updateMode;
	}

	public void setUpdateMode(String updateMode) {
		this.updateMode = updateMode;
	}

	@RDF("provisioning:hasUpdateRate")
	public int getUpdateRate() {
		return updateRate;
	}

	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
}
