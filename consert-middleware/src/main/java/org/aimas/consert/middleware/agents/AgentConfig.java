package org.aimas.consert.middleware.agents;

import org.apache.commons.configuration.Configuration;

/**
 * The configuration of an agent and the methods that allow to get the
 * configuration from a file
 */
public class AgentConfig {

	private String address; // IP address to use for communications with the
							// agent
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
	 * Creates a new AgentConfig for a CtxSensor with values from the given
	 * configuration file
	 * 
	 * @param file the configuration file containing the values for the
	 *            CtxSensor agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxSensorConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxSensor.address"), file.getInt("CtxSensor.port"));
	}

	/**
	 * Creates a new AgentConfig for a CtxUser with values from the given
	 * configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxUser
	 *            agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxUserConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxUser.address"), file.getInt("CtxUser.port"));
	}

	/**
	 * Creates a new AgentConfig for a CtxCoord with values from the given
	 * configuration file
	 * 
	 * @param file the configuration file containing the values for the CtxCoord
	 *            agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxCoordConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxCoord.address"), file.getInt("CtxCoord.port"));
	}

	/**
	 * Creates a new AgentConfig for a CtxQueryHandler with values from the
	 * given configuration file
	 * 
	 * @param file the configuration file containing the values for the
	 *            CtxQueryHandler agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readCtxQueryHandlerConfig(Configuration file) {

		return new AgentConfig(file.getString("CtxQueryHandler.address"), file.getInt("CtxQueryHandler.port"));
	}

	/**
	 * Creates a new AgentConfig for a OrgMgr with values from the given
	 * configuration file
	 * 
	 * @param file the configuration file containing the values for the OrgMgr
	 *            agent
	 * @return a new AgentConfig with the values from the file
	 */
	public static AgentConfig readOrgMgrConfig(Configuration file) {

		return new AgentConfig(file.getString("OrgMgr.address"), file.getInt("OrgMgr.port"));
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
}
