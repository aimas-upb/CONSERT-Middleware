package org.aimas.consert.middleware.model.tasking;

import org.aimas.consert.middleware.model.AgentSpec;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * TaskingCommand from CONSERT protocol ontology
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:TaskingCommand")
public abstract class TaskingCommand {

	private AgentSpec targetAgent;
	
	public TaskingCommand() {}

	@RDF("protocol:hasTargetAgent")
	public AgentSpec getTargetAgent() {
		return targetAgent;
	}

	public void setTargetAgent(AgentSpec targetAgent) {
		this.targetAgent = targetAgent;
	}
}
