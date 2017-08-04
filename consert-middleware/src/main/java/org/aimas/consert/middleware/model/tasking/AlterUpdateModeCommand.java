package org.aimas.consert.middleware.model.tasking;

import java.net.URI;

import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * AlterUpdateModeCommand from CONSERT protocol ontology
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:AlterUpdateModeCommand")
public class AlterUpdateModeCommand extends TaskingCommand {

	private URI targetAssertion;
	private AssertionUpdateMode updateMode;
	
	public AlterUpdateModeCommand() {}

	@RDF("protocol:hasTargetAssertion")
	public URI getTargetAssertion() {
		return targetAssertion;
	}

	public void setTargetAssertion(URI targetAssertion) {
		this.targetAssertion = targetAssertion;
	}

	@RDF("protocol:hasUpdateMode")
	public AssertionUpdateMode getUpdateMode() {
		return updateMode;
	}

	public void setUpdateMode(AssertionUpdateMode updateMode) {
		this.updateMode = updateMode;
	}
}
