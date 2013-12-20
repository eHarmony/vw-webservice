/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola An iterable of examples to be submitted to VW. Also
 *         provides attributes that describe the type/quantity of examples.
 */
public interface ExamplesIterable extends Iterable<Example> {

	/*
	 * Returns the number of examples, or Integer.MAX_VALUE if it's a stream of
	 * examples. This value can be used by components further down the pipeline
	 * to determine whether or not example submission should occur
	 * synchronously.
	 * 
	 * @returns The number of examples (if known) or Integer.MAX_VALUE if
	 * they're being streamed in and the number of examples isn't known ahead of
	 * time. This number can never be < 0.
	 */
	int getNumberOfExamples();

	/*
	 * Returns attributes used to describe the set of examples.
	 */
	String getAttribute(String attributeKey);
}
