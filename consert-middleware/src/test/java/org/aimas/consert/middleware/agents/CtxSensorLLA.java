package org.aimas.consert.middleware.agents;

import java.util.LinkedList;
import java.util.List;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.model.annotations.DefaultAnnotationData;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.LLA;
import org.aimas.consert.tests.hla.assertions.SittingLLA;
import org.aimas.consert.tests.hla.assertions.StandingLLA;
import org.aimas.consert.tests.hla.entities.Person;

public class CtxSensorLLA extends CtxSensor {

	@Override
	protected boolean canSendEvent(ContextAssertion event) {
		return (event instanceof LLA); 
	}

	@Override
	protected void sendAssertionCapabilities() {
		
		Person mihai = new Person("mihai");
		
		AgentAddress ctxSensorAddress = new AgentAddress(this.agentConfig.getAddress(), this.agentConfig.getPort());
		
		AssertionCapability acStanding = new AssertionCapability();
		acStanding.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/Standing");
		acStanding.setContent(new StandingLLA(mihai, new DefaultAnnotationData()));
		acStanding.setProvider(new AgentSpec(ctxSensorAddress, acStanding.getId()));
		
		AssertionCapability acSitting= new AssertionCapability();
		acSitting.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/Sitting");
		acSitting.setContent(new SittingLLA(mihai, new DefaultAnnotationData()));
		acSitting.setProvider(new AgentSpec(ctxSensorAddress, acSitting.getId()));
		
		AssertionCapability acWalking= new AssertionCapability();
		acWalking.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/Walking");
		acWalking.setContent(new StandingLLA(mihai, new DefaultAnnotationData()));
		acWalking.setProvider(new AgentSpec(ctxSensorAddress, acWalking.getId()));
		
		List<AssertionCapability> acs = new LinkedList<AssertionCapability>();
		acs.add(acStanding);
		acs.add(acSitting);
		acs.add(acWalking);
		
		this.sendAssertionCapabilities(acs);
	}
}
