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
	 * An exception occurred when sending predictions back to the client. No
	 * more predictions will be sent. Typically example submission will also be
	 * stopped.
	 */
	PredictionWriteFault,

	/*
	 * Predictions are currently being fetched.
	 */
	OnGoing
}
