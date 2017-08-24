package org.aimas.consert.middleware.agents;

/**
 * The configuration of an agent
 */
public class AgentConfig {

	private String address; // IP address to use for communications with the agent
	private int port; // port to use for communications with the agent

	public AgentConfig() {
		super();
	}

	public AgentConfig(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentConfig other = (AgentConfig) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}
