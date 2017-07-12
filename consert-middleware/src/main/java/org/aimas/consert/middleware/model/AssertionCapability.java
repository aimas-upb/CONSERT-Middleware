package org.aimas.consert.middleware.model;

import java.util.List;

import org.aimas.consert.model.Constants;
import org.aimas.consert.model.annotations.ContextAnnotation;
import org.aimas.consert.model.content.ContextAssertion;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFContainer.ContainerType;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * AssertionCapability from CONSERT protocol ontology
 */
@RDFNamespaces({ "annotation=" + Constants.ANNOTATION_NS,
		"protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#" })
@RDFBean("protocol:AssertionCapability")
public class AssertionCapability extends RDFObject {

	private ContextAssertion content;
	private List<ContextAnnotation> annotations;
	private AgentSpec provider;

	@RDF("protocol:hasContent")
	public ContextAssertion getContent() {
		return content;
	}

	public void setContent(ContextAssertion content) {
		this.content = content;
	}

	@RDF("annotation:hasAnnotation")
	@RDFContainer(ContainerType.LIST)
	public List<ContextAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<ContextAnnotation> annotations) {
		this.annotations = annotations;
	}

	@RDF("protocol:hasProvider")
	public AgentSpec getProvider() {
		return provider;
	}

	public void setProvider(AgentSpec provider) {
		this.provider = provider;
	}
}
