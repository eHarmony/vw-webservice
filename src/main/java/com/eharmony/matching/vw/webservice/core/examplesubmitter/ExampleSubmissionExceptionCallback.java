/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import com.eharmony.matching.vw.webservice.core.ExampleSubmissionException;

/**
 * @author vrahimtoola
 * 
 *         Callback that's fired whenever there's an exception submitting
 *         examples to VW.
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
