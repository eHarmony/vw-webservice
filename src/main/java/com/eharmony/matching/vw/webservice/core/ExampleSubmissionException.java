/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when something bad happens while submitting examples
 *         to VW, or before any examples have been submitted to VW.
 */
public class ExampleSubmissionException extends Exception {

	public ExampleSubmissionException() {
		super();
	}

	public ExampleSubmissionException(String message) {
		super(message);
	}

	public ExampleSubmissionException(Throwable cause) {
		super(cause);
	}

	public ExampleSubmissionException(String message, Throwable cause) {
		super(message, cause);
	}

}
