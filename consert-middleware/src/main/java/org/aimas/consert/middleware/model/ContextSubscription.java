package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * ContextSubscription from CONSERT protocol ontology
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:ContextSubscription")
public class ContextSubscription extends RDFObject {

	private String subscriptionQuery;
	private AgentSpec subscriber;

	public ContextSubscription() {
		this.id = UUID.randomUUID().toString();
	}

	@RDF("protocol:hasSubscriptionQuery")
	public String getSubscriptionQuery() {
		return subscriptionQuery;
	}

	public void setSubscriptionQuery(String subscriptionQuery) {
		this.subscriptionQuery = subscriptionQuery;
	}

	@RDF("protocol:hasSubscriber")
	public AgentSpec getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
}
