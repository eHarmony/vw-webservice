/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when something bad happens when reading predictions
 *         from VW.
 */
public class PredictionFetchException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5193371328499134437L;

	public PredictionFetchException() {
		super();
	}

	public PredictionFetchException(String message) {
		super(message);
	}

	public PredictionFetchException(Throwable cause) {
		super(cause);
	}

	public PredictionFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
