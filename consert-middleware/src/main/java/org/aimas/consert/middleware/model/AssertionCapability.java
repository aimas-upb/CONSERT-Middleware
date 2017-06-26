package org.aimas.consert.middleware.model;

import java.util.UUID;

public class AssertionCapability {

	private Resource content;
	private ContextAnnotation annotation;
	private AgentSpec provider;


	public Resource getContent() {
		return content;
	}

	public void setContent(Resource content) {
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
