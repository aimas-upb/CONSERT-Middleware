	package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;

@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/06/consert/protocol#")
@RDFBean("protocol:AgentAddress")
public class AgentAddress {

	private String ipAddress;
	private int port;
	private String id;
	
	
	@RDF("protocol:ipAddress")
	public String getIpAddress() {
		return ipAddress;
	}
	
	@RDF("protocol:ipAddress")
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	@RDF("protocol:port")
	public int getPort() {
		return port;
	}
	
	@RDF("protocol:port")
	public void setPort(int port) {
		this.port = port;
	}

	@RDFSubject
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
