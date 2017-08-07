package org.aimas.consert.middleware.agents;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

/**
 * The configuration of an agent and the methods that allow to get the
 * configuration from a file
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

	/**
	 * Creates a new AgentConfig for a CtxSensor with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxSensor agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static List<AgentConfig> readCtxSensorConfig(Configuration file) {

		List<Object> listAddresses = file.getList("CtxSensor.address");
		List<Object> listPorts = file.getList("CtxSensor.port");
		
		List<AgentConfig> configs = new ArrayList<AgentConfig>();
		
		String address = null;
		int port;
		
		for(int i = 0 ; i < listAddresses.size() ; i++) {
			
			address = (String) listAddresses.get(i);
			port = Integer.parseInt(((String) listPorts.get(i)));
			
			configs.add(new AgentConfig(address, port));
		}
		
		return configs;
	}

	/**
	 * Creates a new AgentConfig for a CtxUser with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxUser agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxUserConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxUser.address"), file.getInt("CtxUser.port"));
	}

	/**
	 * Creates a new AgentConfig for a CtxCoord with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxCoord agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxCoordConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxCoord.address"), file.getInt("CtxCoord.port"));
	}

	/**
	 * Creates a new AgentConfig for a CtxQueryHandler with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxQueryHandler agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxQueryHandlerConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxQueryHandler.address"), file.getInt("CtxQueryHandler.port"));
	}

	/**
	 * Creates a new AgentConfig for a OrgMgr with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the OrgMgr agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readOrgMgrConfig(Configuration file) {

		return new AgentConfig(file.getString("OrgMgr.address"), file.getInt("OrgMgr.port"));
	}

	/**
	 * Creates a new AgentConfig for a CONSERT Engine with values from the given configuration file
	 * 
	 * @param file the configuration file containing the values for the OrgMgr agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readConsertEngineConfig(Configuration file) {
		
		return new AgentConfig(file.getString("ConsertEngine.address"), file.getInt("ConsertEngine.port"));
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
