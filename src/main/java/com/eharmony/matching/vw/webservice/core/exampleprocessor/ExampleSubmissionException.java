/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when something bad happens while submitting examples
 *         to VW, or before any examples have been submitted to VW.
 */
public class ExampleSubmissionException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5135330791227994409L;

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
