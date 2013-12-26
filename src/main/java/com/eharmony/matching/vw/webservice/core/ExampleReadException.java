/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when there's a problem reading examples submitted to
 *         the web service.
 * 
 *         Technically this isn't a RuntimeException but makes the exception
 *         handling code cleaner.
 */
public class ExampleReadException extends RuntimeException {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1744390625692646099L;

	public ExampleReadException() {
		super();
	}

	public ExampleReadException(String message) {
		super(message);
	}

	public ExampleReadException(Throwable cause) {
		super(cause);
	}

	public ExampleReadException(String message, Throwable cause) {
		super(message, cause);
	}
}
