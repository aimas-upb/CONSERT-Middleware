package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:AgentSpec")
public class AgentSpec extends RDFObject {

	private AgentAddress address;
	private String identifier;
	private String id;
	
	
	@RDF("protocol:hasAddress")
	public AgentAddress getAddress() {
		return address;
	}
	
	public void setAddress(AgentAddress address) {
		this.address = address;
	}
	
	@RDF("protocol:hasIdentifier")
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentSpec other = (AgentSpec) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
}
