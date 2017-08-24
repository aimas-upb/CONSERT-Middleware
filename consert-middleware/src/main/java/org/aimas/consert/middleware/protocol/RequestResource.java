package org.aimas.consert.middleware.protocol;

import java.net.URI;
import java.util.List;

import org.aimas.consert.middleware.model.RDFObject;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * Bean class for resource containing required information to make stateless REST calls
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:RequestResource")
public class RequestResource extends RDFObject {

	private URI resourceURI;  // URI to use to access the resource
	private URI initiatorURI;  // URI to use for communications with the initiator agent
	private URI participantURI;  // URI to use for communications with the participant agent

	private String request;  // content of the request
	private List<BindingSet> result;  // content of the result of the request
	private String stringResult;   // human readable version of the result

	private URI initiatorCallbackURI;  // URI to use for the callback on the initiator
	private RequestState state;  // current state in the protocol

	
	public RequestResource() {
		this.stringResult = null;
	}
	
	
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
	public String getStringResult() {
		
		if(this.stringResult != null) {
			return this.stringResult;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(BindingSet res : this.result) {
			for(Binding b : res) {
				sb.append(b.getName() + " = " + b.getValue().stringValue() + "\n");
			}
		}
		
		return sb.toString();
	}
	
	public void setStringResult(String result) {
		this.stringResult = result;
	}
	
	public List<BindingSet> getResult() {
		return this.result;
	}
	
	public void setResult(List<BindingSet> result) {
		this.result = result;
	}
	
	public boolean hasResultChanged(List<BindingSet> other) {
		
		// First we compare the size of the two sets of results
		if(this.result.size() != other.size()) {
			return true;
		}
		
		// If the two sets have the same size, we compare the elements
		for(BindingSet bsThis : this.result) {
			
			if(!other.contains(bsThis)) {
				return true;
			}
		}
		
		return false;
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
