/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

/**
 * @author vrahimtoola
 * 
 *         Exception thrown when something bad happens when reading predictions
 *         from VW.
 */
public class PredictionFetchException extends Exception {

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
