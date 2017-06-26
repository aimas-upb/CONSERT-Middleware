package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:AssertionCapabilitySubscription")
public class AssertionCapabilitySubscription {

	private String capabilityQuery;
	private AgentSpec subscriber;
	private String id;
	
	
	public AssertionCapabilitySubscription() {
		this.id = UUID.randomUUID().toString();
	}
	
	
	@RDF("protocol:hasCapabilityQuery")
	public String getCapabilityQuery() {
		return capabilityQuery;
	}
	
	@RDF("protocol:hasCapabilityQuery")
	public void setCapabilityQuery(String capabilityQuery) {
		this.capabilityQuery = capabilityQuery;
	}
	
	@RDF("protocol:hasSubscriber")
	public AgentSpec getSubscriber() {
		return subscriber;
	}
	
	@RDF("protocol:hasSubscriber")
	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
	
	@RDFSubject(prefix="protocol:AssertionCapabilitySubscription/")
	public String getId() {
		return id;
	}
}
