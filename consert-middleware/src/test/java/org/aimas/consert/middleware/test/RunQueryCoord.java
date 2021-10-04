package org.aimas.consert.middleware.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.CtxQueryHandler;

/**
 * Starts the HLATest scenario
 */
public class RunQueryCoord {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		// Deploy the OrgMgr agent, which will deploy the other required agent for this scenario
		vertx.deployVerticle(CtxQueryHandler.class.getName(), new DeploymentOptions().setWorker(true));
	}
}
