/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

import com.eharmony.matching.vw.webservice.core.ExamplesIterable;

/**
 * @author vrahimtoola
 * 
 *         Returns an ExampleProcessor.
 */
public interface ExampleProcessorFactory {

	/*
	 * Gets the example processor to use.
	 * 
	 * @param theExamples The VW examples to be submitted.
	 * 
	 * @returns The example processor.
	 */
	ExampleProcessor getExampleProcessor(ExamplesIterable theExamples);
}
