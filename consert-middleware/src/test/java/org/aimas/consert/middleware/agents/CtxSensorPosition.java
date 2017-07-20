package org.aimas.consert.middleware.agents;

import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.Position;

public class CtxSensorPosition extends CtxSensor {

	@Override
	protected boolean canSendEvent(ContextAssertion event) {
		return (event instanceof Position); 
	}
}
