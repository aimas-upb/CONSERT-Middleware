package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:AgentSpec")
public class AgentSpec {

	private AgentAddress address;
	private String identifier;
	
	
	@RDF("protocol:hasAddress")
	public AgentAddress getAddress() {
		return address;
	}
	
	@RDF("protocol:hasAddress")
	public void setAddress(AgentAddress address) {
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
	
	@RDFSubject(prefix="protocol:AgentSpec/")
	public String getId() {
		return identifier + "/" + this.address.getId();
	}
}
