package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDFSubject;

/**
 * Base class for all RDF objects
 */
public abstract class RDFObject {

	protected String id;

	@RDFSubject
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
