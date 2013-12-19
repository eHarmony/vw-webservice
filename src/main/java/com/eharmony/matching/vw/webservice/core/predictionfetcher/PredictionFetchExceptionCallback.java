/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.predictionfetcher;

import com.eharmony.matching.vw.webservice.core.PredictionFetchException;

/**
 * @author vrahimtoola
 * 
 *         Callback that's fired when there's an exception fetching predictions
 *         from VW.
 */
public interface PredictionFetchExceptionCallback {

	/*
	 * Fired whenever an exception occurs when fetching predictions from VW.
	 * 
	 * @param predictionFetcher The prediction fetcher.
	 * 
	 * @param theException The exception that occurred.
	 */
	void onPredictionFetchException(PredictionsIterable predictionFetcher,
			PredictionFetchException theException);
}
