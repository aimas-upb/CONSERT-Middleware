package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * AssertionCapabilitySubscription from CONSERT protocol ontology
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:AssertionCapabilitySubscription")
public class AssertionCapabilitySubscription extends RDFObject {

	private String capabilityQuery;
	private AgentSpec subscriber;

	@RDF("protocol:hasCapabilityQuery")
	public String getCapabilityQuery() {
		return capabilityQuery;
	}

	public void setCapabilityQuery(String capabilityQuery) {
		this.capabilityQuery = capabilityQuery;
	}

	@RDF("protocol:hasSubscriber")
	public AgentSpec getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
}
