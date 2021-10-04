package org.aimas.consert.middleware.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * Starts the HLATest scenario
 */
public class RunSensorLLA {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		// Deploy the OrgMgr agent, which will deploy the other required agent for this scenario
		vertx.deployVerticle(CtxSensorLLA.class.getName(), new DeploymentOptions().setWorker(true));
	}
}
