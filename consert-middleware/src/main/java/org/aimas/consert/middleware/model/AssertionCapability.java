package org.aimas.consert.middleware.model;

public class AssertionCapability {

	private ContextAssertion content;
	private ContextAnnotation annotation;
	private AgentSpec provider;


	public ContextAssertion getContent() {
		return content;
	}

	public void setContent(ContextAssertion content) {
		this.content = content;
	}

	public ContextAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(ContextAnnotation annotation) {
		this.annotation = annotation;
	}

	public AgentSpec getProvider() {
		return provider;
	}

	public void setProvider(AgentSpec provider) {
		this.provider = provider;
	}
}
