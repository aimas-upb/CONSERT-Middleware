package org.aimas.consert.middleware.protocol;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Describes the state of a request
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:RequestState")
public class RequestState {
	
	public static final String REQ_RECEIVED = "req-received";
	public static final String AGREE_SENT = "agree-sent";
	public static final String RESULT_SENT = "result-sent"; 
	
	private String state;
	
	
	public RequestState() {
		this.state = "";
	}
	
	public RequestState(String state) {
		this.state = state;
	}

	
	@RDF("protocol:hasState")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
