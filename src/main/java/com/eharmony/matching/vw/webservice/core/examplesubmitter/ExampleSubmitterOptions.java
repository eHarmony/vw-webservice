/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter;

/**
 * @author vrahimtoola Options for a example submitter. To be passed in to the
 *         ExampleSubmitterFactory when requesting an ExampleSubmitter.
 */
public enum ExampleSubmitterOptions {

	/*
	 * No options specified.
	 */
	None,

	/*
	 * An example submitter that submits examples asynchronously.
	 */
	Async,

	/*
	 * An example submitter that fails on the first failure. If this option
	 * isn't set, then the example submitter can skip bad examples (either upto
	 * some implementation defined limit, or an infinite number of them).
	 */
	FailOnFirstBadExample,

}
