package org.aimas.consert.middleware.protocol;

public class RequestBean {
	private String initiatorURI;
	private String initiatorCallbackURI;
	private String request;
	
	
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
