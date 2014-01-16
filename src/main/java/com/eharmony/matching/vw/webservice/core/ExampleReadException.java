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
 *         Need to make this a RuntimeException since an ExampleReadException
 *         can be thrown from within an iterator, but the iterator interface
 *         doesn't allow you to declare a throws clause in the 'next' method
 *         signature in the implementation.
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
