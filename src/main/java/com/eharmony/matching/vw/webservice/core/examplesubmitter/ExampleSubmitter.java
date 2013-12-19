/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchCompleteCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchExceptionCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionsIterable;

/**
 * @author vrahimtoola Submits examples to VW.
 */
public interface ExampleSubmitter {

	/*
	 * Kicks off the example submission process.
	 * 
	 * @param submissionCallback Callback that's fired after each example is
	 * fired. Can be null.
	 * 
	 * @param submissionCompleteCallback Callback that's fired when the
	 * submitter is done submitting examples. Can be null.
	 * 
	 * @param exceptionCallback The callback to invoke when an exception has
	 * occurred. Can be null. The implementation can decide whether or not to
	 * continue attempting to submit examples after an exception has occurred.
	 * This depends on the ExampleSubmitterOptions specified when the example
	 * submitter was created by the ExampleSubmitterFactory. Those options (or
	 * some subset of those options) may be made available by the
	 * implementation, in the exception message available when the callback is
	 * invoked. But it's up to the implementation to decide if it wants to make
	 * those options available in the exception message or not.
	 * 
	 * @param predictionFetchExceptionCallback A callback that's fired when an
	 * exception has ocurred. Can be null.
	 * 
	 * @param predictionFetchCompleteCallback A callback that's fired after all
	 * predictions have been fetched from VW. Can be null.
	 * 
	 * @returns The prediction fetcher.
	 * 
	 * The manner in which examples are submitted to VW will determine the
	 * manner in fetch the predictions get fetched. i.e, if you submit examples
	 * over TCP-IP, you get the predictions back over the same socket
	 * connection, etc etc.
	 */
	PredictionsIterable submitExamples(
			ExamplesSubmittedCallback submissionCallback,
			ExampleSubmissionCompleteCallback submissionCompleteCallback,
			ExampleSubmissionExceptionCallback exceptionCallback,
			PredictionFetchCompleteCallback predictionFetchCompleteCallback,
			PredictionFetchExceptionCallback predictionFetchExceptionCallback);

}
