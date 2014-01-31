/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import com.eharmony.matching.vw.webservice.common.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.core.ExampleReadException;

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
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleReadException(ExampleProcessingManager exampleProcessingManager, ExampleReadException theException);

	/*
	 * Fired whenever an invalid example is detected.
	 * 
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleFormatException(ExampleProcessingManager exampleProcessingManager, ExampleFormatException theException);

	/*
	 * Fired whenever there's an exception submitting examples.
	 * 
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onExampleSubmissionException(ExampleProcessingManager exampleProcessingManager, ExampleSubmissionException theException);

	/*
	 * Fired when all examples have been submitted.
	 * 
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 */
	void onExampleSubmissionComplete(ExampleProcessingManager exampleProcessingManager);

	/*
	 * Fired whenever there's an exception fetching predictions.
	 * 
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onPredictionFetchException(ExampleProcessingManager exampleProcessingManager, PredictionFetchException theException);

	/*
	 * Fired when all predictions have been fetched.
	 * 
	 * @param exampleProcessingManager The example processing manager that can
	 * be queried to find out more info about the example processing.
	 */
	void onPredictionFetchComplete(ExampleProcessingManager exampleProcessingManager);
}
