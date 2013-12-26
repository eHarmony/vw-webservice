/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;

/**
 * @author vrahimtoola
 * 
 *         Callbacks to be fired when the status of example processing
 *         (submission/prediction fetching) changes.
 */
public interface ExampleProcessingEventHandler {

	/*
	 * Fired whenever there's an exception reading examples.
	 * 
	 * @param processor The example processor that fired this event.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleReadException(ExampleProcessor processor, ExampleReadException theException);

	/*
	 * Fired whenever an invalid example is detected.
	 * 
	 * @param processor The example processor that fired this event.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleFormatException(ExampleProcessor processor, ExampleFormatException theException);

	/*
	 * Fired whenever there's an exception submitting examples.
	 * 
	 * @param processor The example processor that fired this event.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleSubmissionException(ExampleProcessor processor, ExampleSubmissionException theException);

	/*
	 * Fired when all examples have been submitted.
	 * 
	 * @param processor The example processor that fired this event.
	 */
	void onExampleSubmissionComplete(ExampleProcessor processor);

	/*
	 * Fired whenever there's an exception fetching predictions.
	 * 
	 * @param processor The example processor that fired this event.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onPredictionFetchException(ExampleProcessor processor, PredictionFetchException theException);

	/*
	 * Fired when all predictions have been fetched.
	 * 
	 * @param processor The example processor that fired this event.
	 */
	void onPredictionFetchComplete(ExampleProcessor processor);
}
