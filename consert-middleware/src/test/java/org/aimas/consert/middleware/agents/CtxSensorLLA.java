package org.aimas.consert.middleware.agents;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.model.annotations.DefaultAnnotationData;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.LLA;
import org.aimas.consert.tests.hla.assertions.SittingLLA;
import org.aimas.consert.tests.hla.assertions.StandingLLA;
import org.aimas.consert.tests.hla.entities.Person;
import org.aimas.consert.utils.JSONEventReader;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class CtxSensorLLA extends CtxSensor {
	
	private final String EVENTS_FILE_NAME = "files/single_hla_120s_01er_015fd.json";
	
	private ScheduledExecutorService readerService;  // reads the file containing the context assertions
	                                                 // and their annotations
	
	private Queue<Object> events;           // list of the read events
	private Object syncObj = new Object();  // object used for the synchronization of the threads
	

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

	@Override
	protected void readEvents() {
		
		// Start reading the context assertions and their annotations
		ClassLoader classLoader = CtxSensor.class.getClassLoader();
	    File eventsFile = new File(classLoader.getResource(this.EVENTS_FILE_NAME).getFile());

		this.events = JSONEventReader.parseEvents(eventsFile);
		this.events.removeIf(ca -> !(ca instanceof LLA));
		
		this.readerService = Executors.newScheduledThreadPool(1);
		this.readerService.execute(new EventReadTask());
	}
	
	
	private class EventReadTask implements Runnable {
		
		private Repository repo;
		
		public EventReadTask() {
			this.repo = new SailRepository(new MemoryStore());
			this.repo.initialize();
		}
		
		public void run() {
			// get event to be inserted
			ContextAssertion event = (ContextAssertion)events.poll();
			if (event != null) {
				
				// send the event
				System.out.println("CtxSensor " + id + " sends event " + event);
				sendEvent(event);		
				
				// look at the next event if there is one
				ContextAssertion nextEvent = (ContextAssertion)events.peek();
				
				if (nextEvent != null) {
					//long delay = (long)(nextEvent.getStartTimestamp() - event.getStartTimestamp());
					//int delay = 50;
					//System.out.println("Next Event due in " + delay + " ms");
					
					//readerService.schedule(new EventReadTask(), delay, TimeUnit.MILLISECONDS);
					readerService.schedule(new EventReadTask(), 1, TimeUnit.MILLISECONDS);
				}
				else {
					
					synchronized(syncObj) {
						setFinished(true);
					}
				}
			}
			else {
				
				synchronized(syncObj) {
					setFinished(true);
				}
			}
			
			// Tell the CtxCoord that there is no more data
			if(isFinished()) {

				this.repo.shutDown();
				deleteAssertionCapabilities();
				readerService.shutdownNow();
			}
        }
	}
}
