package org.aimas.consert.middleware.model;

import java.net.URI;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * StartUpdatesCommand from CONSERT protocol ontology
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:StartUpdatesCommand")
public class StartUpdatesCommand extends TaskingCommand {

	private URI targetAssertion;
	
	public StartUpdatesCommand() {}

	@RDF("protocol:hasTargetAssertion")
	public URI getTargetAssertion() {
		return targetAssertion;
	}

	public void setTargetAssertion(URI targetAssertion) {
		this.targetAssertion = targetAssertion;
	}
}
