package org.aimas.consert.middleware.test;

import org.aimas.consert.middleware.agents.OrgMgr;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

/**
 * Implementation of the OrgMgr agent for the HLATest scenario
 */
public class OrgMgrImplTest extends OrgMgr {

	@Override
	public void start(Future<Void> future) {

		Future<Void> startFuture = Future.future();
		startFuture.setHandler(handler -> {
			
			// Deploy the agents: a CtxCoord, a CtxSensor for the LLA events, and a CtxSensor for the position events
			DeploymentOptions deplOpt = new DeploymentOptions().setWorker(true);
			
			vertx.deployVerticle(CtxCoordImplTest.class.getName(), deplOpt, result -> {
				
				vertx.deployVerticle(CtxSensorLLA.class.getName(), deplOpt, result2 -> {
					
					vertx.deployVerticle(CtxSensorPosition.class.getName(), deplOpt, result3 -> {
						future.complete();								
					});
				});
			});
		});
		
		super.start(startFuture);
	}
}
