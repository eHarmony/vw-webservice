/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola Callback that's fired when an example is submitted to VW.
 */
public interface ExamplesSubmittedCallback {

	/*
	 * Fired when an example is submitted to VW.
	 * 
	 * @param exampleSubmitter The example submitter.
	 * 
	 * @param theExamples The VW examples submitted.
	 */
	void onExamplesSubmitted(ExampleSubmitter exampleSubmitter,
			Iterable<Example> theExamples);
}
