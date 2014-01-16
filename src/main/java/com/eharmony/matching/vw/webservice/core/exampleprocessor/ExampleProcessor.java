/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

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
	 * @returns An example processing manager that can be used to stop the
	 * example submission process.
	 * 
	 * The manner in which examples are submitted to VW will determine the
	 * manner in which the predictions get fetched. i.e, if you submit examples
	 * over TCP-IP, you get the predictions back over the same socket
	 * connection, etc etc.
	 */
	ExampleProcessingManager submitExamples(ExampleProcessingEventHandler callback) throws ExampleSubmissionException;

	/*
	 * Returns features describing this example processor. The returned object
	 * should never be null.
	 * 
	 * @returns The example processor features provided by the example
	 * processor.
	 */
	ExampleProcessorFeatures getExampleProcessorFeatures();

}
