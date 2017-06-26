package org.aimas.consert.middleware.model;

public class AssertionCapabilitySubscription {

	private String capabilityQuery;
	private AgentSpec subscriber;
	
	
	public String getCapabilityQuery() {
		return capabilityQuery;
	}
	
	public void setCapabilityQuery(String capabilityQuery) {
		this.capabilityQuery = capabilityQuery;
	}
	
	public AgentSpec getSubscriber() {
		return subscriber;
	}
	
	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
}
