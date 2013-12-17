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
public class VWPredictionFetchException extends Exception {

	public VWPredictionFetchException() {
		super();
	}

	public VWPredictionFetchException(String message) {
		super(message);
	}

	public VWPredictionFetchException(Throwable cause) {
		super(cause);
	}

	public VWPredictionFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
