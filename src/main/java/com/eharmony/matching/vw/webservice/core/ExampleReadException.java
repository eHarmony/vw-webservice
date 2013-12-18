/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when there's a problem reading examples submitted to
 *         the web service.
 */
public class ExampleReadException extends Exception {

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
