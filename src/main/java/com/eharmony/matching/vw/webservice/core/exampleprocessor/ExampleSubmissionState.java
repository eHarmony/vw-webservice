/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

/**
 * @author vrahimtoola
 * 
 *         The various states in which the example submission process can be.
 *         This is the most recent state of the example submission process, so
 *         it's possible for instance that there was an exampleformatexception,
 *         and then an examplesubmissionexception but the client only gets to
 *         observe the most recent state which is the example submission fault.
 *         This depends on the implementation of the example submitter, it can
 *         choose to stop submitting examples on an exampleformatexception or
 *         not.
 */
public enum ExampleSubmissionState {

	/*
	 * All examples have been submitted, with no exceptions.
	 */
	Complete,

	/*
	 * Some exception occurred making it impossible to read more examples. No
	 * more examples will be submitted.
	 */
	ExampleReadFault,

	/*
	 * Some exception occurred making it impossible to submit more examples to
	 * VW. No more examples will be submitted.
	 */
	ExampleSubmissionFault,

	/*
	 * One or more examples were invalid. No more examples will be submitted.
	 */
	ExampleFormatFault,

	/*
	 * Examples are currently being submitted.
	 */
	OnGoing

}
