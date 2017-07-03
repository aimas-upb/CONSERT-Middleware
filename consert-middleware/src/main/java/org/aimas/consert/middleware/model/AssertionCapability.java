package org.aimas.consert.middleware.model;

import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFContainer.ContainerType;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces({
	"annotation=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#",
	"protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#"
})
@RDFBean("protocol:AssertionCapability")
public class AssertionCapability {

	private ContextAssertion content;
	private List<ContextAnnotation> annotations;
	private AgentSpec provider;
	private String id;
	

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
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
