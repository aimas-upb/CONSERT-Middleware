package org.aimas.consert.middleware.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Test {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(OrgMgrImplTest.class.getName(), new DeploymentOptions().setWorker(true));
	}
}
