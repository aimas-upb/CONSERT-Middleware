package org.aimas.consert.middleware.agents;

import org.apache.commons.configuration.Configuration;

public class AgentConfig {

	private String address;
	private int port;
	
	
	public AgentConfig() {
		super();
	}
	
	public AgentConfig(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	
	
	public static AgentConfig readCtxSensorConfig(Configuration file) {
		
		return new AgentConfig(file.getString("CtxSensor.address"), file.getInt("CtxSensor.port"));
	}
	
	public static AgentConfig readCtxUserConfig(Configuration file) {
		
		return new AgentConfig(file.getString("CtxUser.address"), file.getInt("CtxUser.port"));
	}
	
	public static AgentConfig readCtxCoordConfig(Configuration file) {
		
		return new AgentConfig(file.getString("CtxCoord.address"), file.getInt("CtxCoord.port"));
	}
	
	public static AgentConfig readCtxQueryHandlerConfig(Configuration file) {
		
		return new AgentConfig(file.getString("CtxQueryHandler.address"), file.getInt("CtxQueryHandler.port"));
	}
	
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
