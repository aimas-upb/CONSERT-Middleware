package org.aimas.consert.middleware.protocol;

import java.net.URI;

import org.aimas.consert.middleware.model.RDFObject;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Bean class for resource containing required information to make stateless REST calls
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:RequestResource")
public class RequestResource extends RDFObject {

	private URI resourceURI; // URI to use to access the resource
	private URI initiatorURI; // URI to use for communications with the initiator agent
	private URI participantURI; // URI to use for communications with the participant agent

	private String request; // content of the request
	private String result; // content of the result of the request

	private URI initiatorCallbackURI; // URI to use for the callback on the initiator
	private RequestState state; // current state in the protocol

	
	@RDF("protocol:hasResourceURI")
	public URI getResourceURI() {
		return resourceURI;
	}

	public void setResourceURI(URI resourceURI) {
		this.resourceURI = resourceURI;
	}

	@RDF("protocol:hasInitiatorURI")
	public URI getInitiatorURI() {
		return initiatorURI;
	}

	public void setInitiatorURI(URI initiatorURI) {
		this.initiatorURI = initiatorURI;
	}

	@RDF("protocol:hasParticipantURI")
	public URI getParticipantURI() {
		return participantURI;
	}

	public void setParticipantURI(URI participantURI) {
		this.participantURI = participantURI;
	}

	@RDF("protocol:hasRequest")
	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	@RDF("protocol:hasResult")
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@RDF("protocol:hasInitiatorCallbackURI")
	public URI getInitiatorCallbackURI() {
		return initiatorCallbackURI;
	}

	public void setInitiatorCallbackURI(URI initiatorCallbackURI) {
		this.initiatorCallbackURI = initiatorCallbackURI;
	}

	@RDF("protocol:hasState")
	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}
}
