package org.aimas.consert.middleware.protocol;

import java.net.URI;

import org.aimas.consert.middleware.model.ContextSubscription;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Bean class containing all the parameters to transmit for a context subscription request
 */
@RDFNamespaces("protocol=http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#")
@RDFBean("protocol:ContextSubscriptionRequest")
public class ContextSubscriptionRequest {
	
	private URI initiatorURI;  // URI to use to communicate with the initiator
	private URI initiatorCallbackURI;  // URI to use for the callback on the initiator
	private ContextSubscription ctxSubs;  // The content of the context subscription

	public ContextSubscriptionRequest() {}

	public ContextSubscriptionRequest(URI initiatorURI, URI initiatorCallbackURI, ContextSubscription ctxSubs) {
		
		this.initiatorURI = initiatorURI;
		this.initiatorCallbackURI = initiatorCallbackURI;
		this.ctxSubs = ctxSubs;
	}

	@RDF("protocol:hasInitiatorURI")
	public URI getInitiatorURI() {
		return initiatorURI;
	}

	public void setInitiatorURI(URI initiatorURI) {
		this.initiatorURI = initiatorURI;
	}

	@RDF("protocol:hasInitiatorCallbackURI")
	public URI getInitiatorCallbackURI() {
		return initiatorCallbackURI;
	}

	public void setInitiatorCallbackURI(URI initiatorCallbackURI) {
		this.initiatorCallbackURI = initiatorCallbackURI;
	}

	@RDF("protocol:hasContextSubscription")
	public ContextSubscription getContextSubscription() {
		return ctxSubs;
	}

	public void setContextSubscription(ContextSubscription ctxSubs) {
		this.ctxSubs = ctxSubs;
	}

	@Override
	public String toString() {
		return "ContextSubscriptionRequest [initiatorURI=" + initiatorURI + ", initiatorCallbackURI="
				+ initiatorCallbackURI + ", ctxSubs=" + ctxSubs + "]";
	}
}
