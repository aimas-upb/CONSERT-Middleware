package org.aimas.consert.middleware.agents;

import org.eclipse.rdf4j.repository.Repository;

/**
 * Interface to define an agent
 */
public interface Agent {

	/**
	 * Gives the repository that contains all the RDF statements representing the data
	 * @return the repository used by the agent
	 */
	Repository getRepository();
}
