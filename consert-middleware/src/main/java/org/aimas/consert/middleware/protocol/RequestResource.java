package org.aimas.consert.middleware.protocol;

/**
 * Bean class for resource containing required information to make stateless REST calls
 */
public class RequestResource {

	private String resourceURI;
	private String initiatorURI;
	private String participantURI;
	
	private String request;
	private String result;
	
	private String initiatorCallbackURI;
	private RequestState state;
	
	
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	public String getInitiatorURI() {
		return initiatorURI;
	}
	public void setInitiatorURI(String initiatorURI) {
		this.initiatorURI = initiatorURI;
	}
	public String getParticipantURI() {
		return participantURI;
	}
	public void setParticipantURI(String participantURI) {
		this.participantURI = participantURI;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getInitiatorCallbackURI() {
		return initiatorCallbackURI;
	}
	public void setInitiatorCallbackURI(String initiatorCallbackURI) {
		this.initiatorCallbackURI = initiatorCallbackURI;
	}
	public RequestState getState() {
		return state;
	}
	public void setState(RequestState state) {
		this.state = state;
	}
}
