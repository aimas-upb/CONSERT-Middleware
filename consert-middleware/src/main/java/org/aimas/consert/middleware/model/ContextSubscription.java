package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:ContextSubscription")
public class ContextSubscription {

	private String subscriptionQuery;
	private AgentSpec subscriber;
	private String id;
	
	
	public ContextSubscription() {
		this.id = UUID.randomUUID().toString();
	}
	
	
	@RDF("protocol:hasSubscriptionQuery")
	public String getSubscriptionQuery() {
		return subscriptionQuery;
	}
	
	@RDF("protocol:hasSubscriptionQuery")
	public void setSubscriptionQuery(String subscriptionQuery) {
		this.subscriptionQuery = subscriptionQuery;
	}
	
	@RDF("hasSubscriber")
	public AgentSpec getSubscriber() {
		return subscriber;
	}
	
	@RDF("hasSubscriber")
	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
