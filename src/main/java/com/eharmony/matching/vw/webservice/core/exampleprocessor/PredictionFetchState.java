/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor;

/**
 * @author vrahimtoola
 * 
 *         The states in which the prediction fetch process can be.
 */
public enum PredictionFetchState {

	/*
	 * All predictions fetched with no exceptions.
	 */
	Complete,

	/*
	 * Some exception occurred when reading predictions from VW. No more
	 * predictions will be read from VW.
	 */
	PredictionFetchFault,

	/*
	 * Predictions are currently being fetched.
	 */
	OnGoing
}
