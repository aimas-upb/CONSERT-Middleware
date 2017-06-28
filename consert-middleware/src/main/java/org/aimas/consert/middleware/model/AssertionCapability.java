package org.aimas.consert.middleware.model;

import java.net.URI;
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

	private URI content;
	private List<URI> annotations;
	private URI provider;
	private String id;
	

	@RDF("protocol:hasContent")
	public URI getContent() {
		return content;
	}

	@RDF("protocol:hasContent")
	public void setContent(URI content) {
		this.content = content;
	}

	@RDF("annotation:hasAnnotation")
	@RDFContainer(ContainerType.SEQ)
	public List<URI> getAnnotations() {
		return annotations;
	}

	@RDF("annotation:hasAnnotation")
	@RDFContainer(ContainerType.SEQ)
	public void setAnnotations(List<URI> annotations) {
		this.annotations = annotations;
	}

	@RDF("protocol:hasProvider")
	public URI getProvider() {
		return provider;
	}

	@RDF("protocol:hasProvider")
	public void setProvider(URI provider) {
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
