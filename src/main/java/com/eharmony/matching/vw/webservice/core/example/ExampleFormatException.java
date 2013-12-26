/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.example;

/**
 * @author vrahimtoola
 * 
 *         Exception indicating that the format of an example isn't valid.
 *         Examples must be in the proper VW format, after all. I'm making this
 *         a subclass of RuntimeException as it indicates a programmer error,
 *         similar to NumberFormatException.
 */
public class ExampleFormatException extends IllegalArgumentException {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6238484930971388916L;

	public ExampleFormatException() {
		super();
	}

	public ExampleFormatException(String message) {
		super(message);
	}

	public ExampleFormatException(Throwable cause) {
		super(cause);
	}

	public ExampleFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
