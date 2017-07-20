package org.aimas.consert.middleware.agents;

import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.LLA;

public class CtxSensorLLA extends CtxSensor {

	@Override
	protected boolean canSendEvent(ContextAssertion event) {
		return (event instanceof LLA); 
	}
}
