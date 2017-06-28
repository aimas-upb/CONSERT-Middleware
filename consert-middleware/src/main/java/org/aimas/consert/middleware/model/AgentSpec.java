package org.aimas.consert.middleware.model;

import java.net.URI;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:AgentSpec")
public class AgentSpec {

	private URI address;
	private String identifier;
	private String id;
	
	
	@RDF("protocol:hasAddress")
	public URI getAddress() {
		return address;
	}
	
	@RDF("protocol:hasAddress")
	public void setAddress(URI address) {
		this.address = address;
	}
	
	@RDF("protocol:hasIdentifier")
	public String getIdentifier() {
		return identifier;
	}
	
	@RDF("protocol:hasIdentifier")
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
