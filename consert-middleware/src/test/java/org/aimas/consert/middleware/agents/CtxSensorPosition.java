package org.aimas.consert.middleware.agents;

import java.util.LinkedList;
import java.util.List;

import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.model.annotations.DefaultAnnotationData;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.Position;
import org.aimas.consert.tests.hla.entities.Area;
import org.aimas.consert.tests.hla.entities.Person;

public class CtxSensorPosition extends CtxSensor {

	@Override
	protected boolean canSendEvent(ContextAssertion event) {
		return (event instanceof Position); 
	}

	@Override
	protected void sendAssertionCapabilities() {
		
		Person mihai = new Person("mihai");		
		
		AgentAddress ctxSensorAddress = new AgentAddress(this.agentConfig.getAddress(), this.agentConfig.getPort());
		
		AssertionCapability acWork = new AssertionCapability();
		acWork.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/WorkArea");
		acWork.setContent(new Position(mihai, new Area("WORK_AREA"), new DefaultAnnotationData()));
		acWork.setProvider(new AgentSpec(ctxSensorAddress, acWork.getId()));
		
		AssertionCapability acConference = new AssertionCapability();
		acConference.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/ConferenceArea");
		acConference.setContent(new Position(mihai, new Area("CONFERENCE_AREA"), new DefaultAnnotationData()));
		acConference.setProvider(new AgentSpec(ctxSensorAddress, acConference.getId()));
		
		AssertionCapability acExercise = new AssertionCapability();
		acExercise.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/ExerciseArea");
		acExercise.setContent(new Position(mihai, new Area("EXERCISE_AREA"), new DefaultAnnotationData()));
		acExercise.setProvider(new AgentSpec(ctxSensorAddress, acExercise.getId()));
		
		AssertionCapability acDining = new AssertionCapability();
		acDining.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/DiningArea");
		acDining.setContent(new Position(mihai, new Area("DINING_AREA"), new DefaultAnnotationData()));
		acDining.setProvider(new AgentSpec(ctxSensorAddress, acDining.getId()));
		
		List<AssertionCapability> acs = new LinkedList<AssertionCapability>();
		acs.add(acWork);
		acs.add(acConference);
		acs.add(acExercise);
		acs.add(acDining);
		
		this.sendAssertionCapabilities(acs);
	}
}
