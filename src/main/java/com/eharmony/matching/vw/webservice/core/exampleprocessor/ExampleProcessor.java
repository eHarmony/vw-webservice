/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import com.eharmony.matching.vw.webservice.core.prediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         Submits examples to VW.
 */
public interface ExampleProcessor {

	/*
	 * Kicks off the example submission process.
	 * 
	 * @param callback A callback handler to handle various status changes as
	 * examples are being processed.
	 * 
	 * @returns The iterable of predictions. If the processor is already in the
	 * process of submitting examples, a runtime exception may be thrown.
	 * 
	 * The manner in which examples are submitted to VW will determine the
	 * manner in which the predictions get fetched. i.e, if you submit examples
	 * over TCP-IP, you get the predictions back over the same socket
	 * connection, etc etc.
	 */
	Iterable<Prediction> submitExamples(ExampleProcessingEventHandler callback) throws ExampleSubmissionException;

	/*
	 * Returns features describing this example processor. The returned object
	 * should never be null.
	 * 
	 * @returns The example processor features provided by the example
	 * processor.
	 */
	ExampleProcessorFeatures getExampleSubmitterFeatures();

	/*
	 * Gets the total number of examples submitted thus far.
	 * 
	 * @returns The total number of examples submitted thus far.
	 */
	long getTotalNumberOfExamplesSubmitted();

	/*
	 * Gets the total number of examples skipped thus far. An example can be
	 * skipped if it's format is invalid, for instance.
	 * 
	 * @returns The total number of skipped examples.
	 */
	long getTotalNumberOfExamplesSkipped();

	/*
	 * Gets the current state of example submission.
	 * 
	 * @returns The current example submission state.
	 */
	ExampleSubmissionState getExampleSubmissionState();

	/*
	 * Gets the current state of prediction fetching.
	 * 
	 * @returns The current prediction fetching state.
	 */
	PredictionFetchState getPredictionFetchState();
}
