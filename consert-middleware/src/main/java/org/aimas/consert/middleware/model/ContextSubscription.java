package org.aimas.consert.middleware.model;

public class ContextSubscription {

	private String subscriptionQuery;
	private AgentSpec subscriber;
	
	
	public String getSubscriptionQuery() {
		return subscriptionQuery;
	}
	
	public void setSubscriptionQuery(String subscriptionQuery) {
		this.subscriptionQuery = subscriptionQuery;
	}
	
	public AgentSpec getSubscriber() {
		return subscriber;
	}
	
	public void setSubscriber(AgentSpec subscriber) {
		this.subscriber = subscriber;
	}
}
