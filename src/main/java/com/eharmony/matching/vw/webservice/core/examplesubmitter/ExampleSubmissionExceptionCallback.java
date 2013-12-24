/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import com.eharmony.matching.vw.webservice.core.ExampleSubmissionException;

/**
 * @author vrahimtoola
 * 
 *         Callback that's fired whenever there's an exception submitting
 *         examples to VW. This callback is intended to be used when some
 *         examples have already been submitted, and then some exception occurs
 *         preventing more examples from being submitted.
 * 
 *         If an exception occurs before any examples are submitted, the example
 *         submitter's submitExamples method will itself throw an
 *         ExampleSubmissionException.
 */
public interface ExampleSubmissionExceptionCallback {

	/*
	 * Fired whenever an exception occurs when submitting examples to VW.
	 * 
	 * @param exampleSubmitter The example submitter.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleSubmissionException(ExampleSubmitter exampleSubmitter,
			ExampleSubmissionException theException);
}
