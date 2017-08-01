package org.aimas.consert.middleware.protocol;

/**
 * Bean class containing all the parameters to transmit in a request
 */
public class RequestBean {
	private String initiatorURI; // URI to use to communicate with the initiator
	private String initiatorCallbackURI; // URI to use for the callback on the
											// initiator
	private String request; // The content of the request

	public RequestBean() {
		super();
	}

	public RequestBean(String initiatorURI, String initiatorCallbackURI, String request) {
		super();
		this.initiatorURI = initiatorURI;
		this.initiatorCallbackURI = initiatorCallbackURI;
		this.request = request;
	}

	public String getInitiatorURI() {
		return initiatorURI;
	}

	public void setInitiatorURI(String initiatorURI) {
		this.initiatorURI = initiatorURI;
	}

	public String getInitiatorCallbackURI() {
		return initiatorCallbackURI;
	}

	public void setInitiatorCallbackURI(String initiatorCallbackURI) {
		this.initiatorCallbackURI = initiatorCallbackURI;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}
}
