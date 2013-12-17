/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when something bad happens while submitting examples
 *         to VW.
 */
public class VWExampleSubmissionException extends Exception {

	public VWExampleSubmissionException() {
		super();
	}

	public VWExampleSubmissionException(String message) {
		super(message);
	}

	public VWExampleSubmissionException(Throwable cause) {
		super(cause);
	}

	public VWExampleSubmissionException(String message, Throwable cause) {
		super(message, cause);
	}
}
