package org.aimas.consert.middleware.model;

import java.util.UUID;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces({
	"annotation=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#",
	"protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#"
})
@RDFBean("protocol:AssertionCapability")
public class AssertionCapability {

	private ContextAssertion content;
	private ContextAnnotation annotation;
	private AgentSpec provider;
	private String id;

	
	public AssertionCapability() {
		this.id = UUID.randomUUID().toString();
	}
	

	@RDF("protocol:hasContent")
	public ContextAssertion getContent() {
		return content;
	}

	@RDF("protocol:hasContent")
	public void setContent(ContextAssertion content) {
		this.content = content;
	}

	@RDF("annotation:hasAnnotation")
	public ContextAnnotation getAnnotation() {
		return annotation;
	}

	@RDF("annotation:hasAnnotation")
	public void setAnnotation(ContextAnnotation annotation) {
		this.annotation = annotation;
	}

	@RDF("protocol:hasProvider")
	public AgentSpec getProvider() {
		return provider;
	}

	@RDF("protocol:hasProvider")
	public void setProvider(AgentSpec provider) {
		this.provider = provider;
	}
	
	@RDFSubject(prefix="protocol:AssertionCapability/")
	public String getId() {
		return this.id;
	}
}
