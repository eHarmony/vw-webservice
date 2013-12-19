/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.predictionfetcher;

import java.math.BigInteger;

/**
 * @author vrahimtoola Callback that's fired when all predictions have been
 *         fetched from VW.
 */
public interface PredictionFetchCompleteCallback {

	/*
	 * Fired after all predictions have been fetched from VW.
	 * 
	 * @param predictionFetcher The prediction fetcher.
	 * 
	 * @param numPredictions The total number of predictions fetched from VW.
	 */
	void onAllPredictionsFetched(PredictionsIterable predictionFetcher,
			BigInteger numPredictions);
}
