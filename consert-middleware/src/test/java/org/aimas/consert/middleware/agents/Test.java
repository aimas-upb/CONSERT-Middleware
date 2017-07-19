package org.aimas.consert.middleware.agents;

import io.vertx.core.Vertx;

public class Test {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(CtxCoord.class.getName(), res -> {
			vertx.deployVerticle(CtxSensor.class.getName());
		});
	}
}
