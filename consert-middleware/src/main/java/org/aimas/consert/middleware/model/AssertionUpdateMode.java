package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * AssertionUpdateMode from CONSERT provisioning ontology
 */
@RDFNamespaces("provisioning=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/coordconf#")
@RDFBean("provisioning:AssertionUpdateMode")
public class AssertionUpdateMode {

	private String updateMode;  // time-based or change-based
	private int updateRate;  // for time-based update mode only
	
	public AssertionUpdateMode() {}

	public String getUpdateMode() {
		return updateMode;
	}

	public void setUpdateMode(String updateMode) {
		this.updateMode = updateMode;
	}

	public int getUpdateRate() {
		return updateRate;
	}

	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
}
