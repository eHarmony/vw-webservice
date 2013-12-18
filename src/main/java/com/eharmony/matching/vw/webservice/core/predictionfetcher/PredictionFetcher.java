/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.predictionfetcher;

import com.eharmony.matching.vw.webservice.core.vwprediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         Fetches predictions from VW.
 */
public interface PredictionFetcher {

	/*
	 * Fetches the raw predictions from VW. Depending on the PredictionFetcher
	 * options in effect for this fetcher, this method might block (if
	 * PredictionFetcherOptions.FetchAllAtOnce is in effect).
	 * 
	 * @returns An iterable containing the predictions.
	 */
	Iterable<Prediction> fetchPredictions();

}
