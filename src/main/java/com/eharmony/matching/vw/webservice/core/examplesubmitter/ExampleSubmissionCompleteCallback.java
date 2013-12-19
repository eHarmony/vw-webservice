/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import java.math.BigInteger;

/**
 * @author vrahimtoola Callback that's fired after all examples have been
 *         submitted to VW.
 */
public interface ExampleSubmissionCompleteCallback {

	/*
	 * Fired after all examples have been submitted to VW. Note that this
	 * callback can also be fired after the example submitter found a bad
	 * example and decided to stop submitting more examples to VW.
	 * 
	 * @param exampleSubmitter The example submitter.
	 * 
	 * @param numberOfSubmittedExamples The number of examples that were in fact
	 * submitted to VW.
	 */
	void onAllExamplesSubmitted(ExampleSubmitter exampleSubmitter,
			BigInteger numberOfSubmittedExamples);
}
