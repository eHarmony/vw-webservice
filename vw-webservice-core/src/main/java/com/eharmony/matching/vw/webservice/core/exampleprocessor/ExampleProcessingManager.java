/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import com.eharmony.matching.vw.webservice.common.prediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         The example processing manager can be used to stop the example
 *         submission process and retrieve a forward-only iterable of
 *         predictions.
 */
public interface ExampleProcessingManager {

	/*
	 * Returns the iterable of predictions.
	 * 
	 * @returns The iterable of predictions.
	 */
	Iterable<Prediction> getPredictionsIterable();

	/*
	 * Stops the example submission process, if it's still ongoing. If it has
	 * already been stopped, has no effect. Prediction fetching will continue
	 * until there are no more predictions to be fetched from VW, ie, the
	 * iterable returned from 'getPredictionsIterable()' returns no more
	 * predictions.
	 */
	void stopAll();

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
	 * Gets the total number of predictions fetched from VW.
	 * 
	 * @returns The total number of predictions fetched from VW.
	 */
	long getTotalNumberOfPredictionsFetched();

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
