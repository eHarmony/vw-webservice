/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import java.util.Map;

/**
 * @author vrahimtoola
 * 
 *         Features and other stuff describing the example processor. Feature
 *         keys cannot be null, but feature values may be null.
 */
public interface ExampleProcessorFeatures {

	/*
	 * Whether or not the example processor's submitExamples() method will
	 * execute synchronously.
	 * 
	 * @returns True if the example processor submits examples asynchronously,
	 * false otherwise.
	 */
	boolean isAsync();

	/*
	 * Returns all the features applicable to this example processor.
	 * 
	 * @returns All the features that this processor provides. None of the keys
	 * can be null, but values may be. The types of the values should be
	 * documented by the example processor. The returned map should never be
	 * null, but can be empty.
	 */
	Map<String, Object> getAllFeatures();

}
