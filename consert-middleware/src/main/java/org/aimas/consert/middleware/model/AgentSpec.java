package org.aimas.consert.middleware.model;

public class AgentSpec {

	private AgentAddress address;
	private String identifier;
	
	
	public AgentAddress getAddress() {
		return address;
	}
	
	public void setAddress(AgentAddress address) {
		this.address = address;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
