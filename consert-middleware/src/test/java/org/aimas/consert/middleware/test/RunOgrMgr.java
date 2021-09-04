package org.aimas.consert.middleware.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.aimas.consert.middleware.agents.OrgMgr;

/**
 * Starts the HLATest scenario
 */
public class RunOgrMgr {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		// Deploy the OrgMgr agent, which will deploy the other required agent for this scenario
		vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true));
	}
}
