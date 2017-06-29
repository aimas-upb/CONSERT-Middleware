package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:ContextQuery")
public class ContextQuery {

	private String assertionQuery;
	private AgentSpec queryAgent;
	private String id;
	
	
	@RDF("protocol:hasAssertionQuery")
	public String getAssertionQuery() {
		return assertionQuery;
	}
	
	@RDF("protocol:hasAssertionQuery")
	public void setAssertionQuery(String assertionQuery) {
		this.assertionQuery = assertionQuery;
	}
	
	@RDF("protocol:hasQueryAgent")
	public AgentSpec getQueryAgent() {
		return queryAgent;
	}
	
	@RDF("protocol:hasQueryAgent")
	public void setQueryAgent(AgentSpec queryAgent) {
		this.queryAgent = queryAgent;
	}
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}