package org.aimas.consert.middleware.config;

import java.net.URI;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * Defines a sensing policy according to the deployment ontology
 */
@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxSensorPolicy")
public class SensingPolicy extends AgentPolicy {
	
	private URI contextAssertionRes;
	private String assertionAdaptorClass;
	
	
	public SensingPolicy() {}
	
	public SensingPolicy(String fileNameOrURI, URI contextAssertion, String assertionAdaptorClass) {
		super(fileNameOrURI);
		
		this.contextAssertionRes = contextAssertion;
		this.assertionAdaptorClass = assertionAdaptorClass;
	}

	/**
	 * @return The {@link URI} defining the ContextAssertion for which this SensorPolicy is set
	 */
	@RDF("orgconf:forContextAssertion")
    public URI getContextAssertionRes() {
	    return contextAssertionRes;
    }
	
	public void setContextAssertionRes(URI contextAssertionRes) {
		this.contextAssertionRes = contextAssertionRes;
	}

	/**
	 * @return The implementation class for the SensorAdaptor service which interfaces with the underlying
	 * sensor management middleware in order to generate ContextAssertion data and handle TASKING commands.
	 */
	@RDF("orgconf:usesAssertionAdaptor")
    public String getAssertionAdaptorClass() {
	    return assertionAdaptorClass;
    }
	
	public void setAssertionAdaptorClass(String assertionAdaptorClass) {
		this.assertionAdaptorClass = assertionAdaptorClass;
	}
}
