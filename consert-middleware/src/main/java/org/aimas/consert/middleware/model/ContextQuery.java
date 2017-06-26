package org.aimas.consert.middleware.model;

public class ContextQuery {

	private String assertionQuery;
	private AgentSpec queryAgent;
	
	
	public String getAssertionQuery() {
		return assertionQuery;
	}
	
	public void setAssertionQuery(String assertionQuery) {
		this.assertionQuery = assertionQuery;
	}
	
	public AgentSpec getQueryAgent() {
		return queryAgent;
	}
	
	public void setQueryAgent(AgentSpec queryAgent) {
		this.queryAgent = queryAgent;
	}
}
