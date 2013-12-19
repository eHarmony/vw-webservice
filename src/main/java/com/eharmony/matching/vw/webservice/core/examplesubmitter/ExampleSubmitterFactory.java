/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import com.eharmony.matching.vw.webservice.core.ExamplesIterable;

/**
 * @author vrahimtoola Returns and ExampleSubmitter.
 */
public interface ExampleSubmitterFactory {

	/*
	 * Gets the example submitter to use.
	 * 
	 * @param theExamples The VW examples to be submitted.
	 * 
	 * @param options Options for the type of example submitter that's desired.
	 * The factory implementation should create an example submitter that can
	 * meet as many of the specified options as possible. The returned
	 * exampleSubmitter's getExampleSubmissionOptions() method can be used to
	 * see which options it will in fact honor.
	 */
	ExampleSubmitter getExampleSubmitter(ExamplesIterable theExamples);
}
