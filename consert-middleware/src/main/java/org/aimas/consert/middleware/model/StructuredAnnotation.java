package org.aimas.consert.middleware.model;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

/**
 * StructuredAnnotation from CONSERT ontology
 */
@RDFNamespaces("ann=http://pervasive.semanticweb.org/ont/2014/05/consert/annotation#")
@RDFBean("ann:StructuredAnnotation")
public abstract class StructuredAnnotation extends ContextAnnotation {

}
