/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.prediction;

/**
 * @author vrahimtoola Creates a Prediction from a string. VW returns
 *         predictions as simple strings so we want a way to convert that string
 *         into a proper Prediction.
 */
public interface VWPredictionConverter {

	/*
	 * Creates a Prediction from a string.
	 */
	Prediction fromString(String thePrediction);
}
