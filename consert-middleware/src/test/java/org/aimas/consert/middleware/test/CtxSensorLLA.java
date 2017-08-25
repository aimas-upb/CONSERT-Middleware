package org.aimas.consert.middleware.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aimas.consert.middleware.agents.CtxSensor;
import org.aimas.consert.middleware.config.AgentSpecification;
import org.aimas.consert.middleware.config.CMMAgentContainer;
import org.aimas.consert.middleware.config.MiddlewareConfig;
import org.aimas.consert.middleware.config.SensorSpecification;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AgentSpec;
import org.aimas.consert.middleware.model.AssertionCapability;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.model.content.ContextAssertion;
import org.aimas.consert.tests.hla.assertions.LLA;
import org.aimas.consert.utils.JSONEventReader;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import io.vertx.core.Future;

/**
 * Implementation of the CtxSensor agent that reads LLA events for the HLATest scenario
 */
public class CtxSensorLLA extends CtxSensor {
	
	// name of the file that contains the LLA events
	private static final String EVENTS_FILE_NAME = "files/single_hla_120s_01er_015fd.json";
	
	
	private ScheduledExecutorService readerService;  // reads the file containing the context assertions
	                                                 // and their annotations
	
	private Queue<Object> events;  // list of the read events
	private Object syncObj = new Object();  // object used for the synchronization of the threads
	
	
	@Override
	public void start(Future<Void> future) {

		List<AgentSpecification> orgMgrSpecs = MiddlewareConfig.readAgentConfigList(SensorSpecification.class,
			"http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#CtxSensorSpec");
		AgentSpecification spec = null;
		
		// Get the configuration for the CtxSensor that reads LLAs
		for(AgentSpecification as : orgMgrSpecs) {
			if(as.getAgentLocalName().contains("LLA")) {
				spec = as;
			}
		}
		
		// Get the configuration of the OrgMgr agent
		if(spec != null) {
			CMMAgentContainer container = spec.getAgentAddress().getAgentContainer();
			this.orgMgr = new AgentAddress(container.getContainerHost(), container.getContainerPort());
		} else {
			// use a default value
			this.orgMgr = new AgentAddress("127.0.0.1", 8080);
		}
		
		super.start(future);
	}

	@Override
	protected void sendAssertionCapabilities(Future<Void> future) {
		
		AgentAddress ctxSensorAddress = new AgentAddress(this.agentConfig.getAddress(), this.agentConfig.getPort());
		
		// Send the assertion capability for LLA events
		AssertionCapability ac = new AssertionCapability();
		ac.setProvider(new AgentSpec(ctxSensorAddress, ac.getId()));
		ac.setId("http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapability/LLA");
		try {
			ac.setContent(new URI("http://example.org/hlatest/LLA"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		List<AssertionCapability> acs = new ArrayList<AssertionCapability>();
		acs.add(ac);
		
		this.sendAssertionCapabilities(acs, future);
	}

	@Override
	protected void readEvents() {
		
		// Start reading the context assertions and their annotations
		ClassLoader classLoader = CtxSensor.class.getClassLoader();
	    File eventsFile = new File(classLoader.getResource(CtxSensorLLA.EVENTS_FILE_NAME).getFile());

	    // Keep the LLA events only
		this.events = JSONEventReader.parseEvents(eventsFile);
		this.events.removeIf(ca -> !(ca instanceof LLA));
		
		this.readerService = Executors.newScheduledThreadPool(1);
		this.readerService.execute(new EventReadTask());
	}
	
	
	/**
	 * Allows to read an event and to send it to the CtxCoord agent
	 */
	private class EventReadTask implements Runnable {
		
		private Repository repo;  // contains the RDF statements for the events
		
		public EventReadTask() {
			this.repo = new SailRepository(new MemoryStore());
			this.repo.initialize();
		}
		
		public void run() {

			URI uri = URI.create("http://example.org/hlatest/LLA");
			
			// if the LLA updates are enabled, read the next event and send it 
			if(updateModes.containsKey(uri)) {
			
				// get event to be inserted
				ContextAssertion event = (ContextAssertion)events.poll();
				if (event != null) {
					
					// send the event
					sendEvent(event);		
					
					// look at the next event if there is one
					ContextAssertion nextEvent = (ContextAssertion)events.peek();
					
					if (nextEvent != null) {
						
						AssertionUpdateMode updateMode = updateModes.get(uri).getUpdateMode();
						long delay = 1;
						
						// if the updates are time-based, skip all the events until the closest after the update rate
						if(updateMode.getUpdateMode() == AssertionUpdateMode.TIME_BASED) {
							
							boolean nextAvailable = false;
							
							while(!nextAvailable) {
								nextEvent = (ContextAssertion)events.peek();
								
								if(nextEvent != null
									&& nextEvent.getStartTimestamp() - event.getStartTimestamp()
										< updateMode.getUpdateRate()){
									
									nextEvent = (ContextAssertion)events.poll();
								} else {
									nextAvailable = true;
								}
							}
							
							delay = updateMode.getUpdateRate();
							
						// if the updates are change-based, skip all the events until the next change of type or person
						} else {
							
							boolean changeDetected = false;
							
							while(!changeDetected) {
								nextEvent = (ContextAssertion)events.peek();
								
								if(nextEvent != null
										&& ((LLA) nextEvent).getPerson().equals(((LLA) event).getPerson())
										&& ((LLA) nextEvent).getType().equals(((LLA) event).getType())
										&& nextEvent.getClass().equals(event.getClass())) {
									
									nextEvent = (ContextAssertion)events.poll();
								} else {
									changeDetected = true;
								}
							}
							
							if(nextEvent != null) {
								delay = (long)(nextEvent.getStartTimestamp() - event.getStartTimestamp());
							} else {
								delay = 1;
							}
						}

						System.out.println("Next Event due in " + delay + " ms");
						delay = 1; // for test only
						readerService.schedule(new EventReadTask(), delay, TimeUnit.MILLISECONDS);
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
			} else {
				// if the updates for LLAs are not enabled, wait before checking again
				readerService.schedule(new EventReadTask(), 100, TimeUnit.MILLISECONDS);
			}
        }
	}
}
