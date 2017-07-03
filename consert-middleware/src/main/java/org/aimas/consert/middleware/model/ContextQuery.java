package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:ContextQuery")
public class ContextQuery extends RDFObject {

	private String assertionQuery;
	private AgentSpec queryAgent;
	
	
	@RDF("protocol:hasAssertionQuery")
	public String getAssertionQuery() {
		return assertionQuery;
	}
	
	public void setAssertionQuery(String assertionQuery) {
		this.assertionQuery = assertionQuery;
	}
	
	@RDF("protocol:hasQueryAgent")
	public AgentSpec getQueryAgent() {
		return queryAgent;
	}
	
	public void setQueryAgent(AgentSpec queryAgent) {
		this.queryAgent = queryAgent;
	}
}
