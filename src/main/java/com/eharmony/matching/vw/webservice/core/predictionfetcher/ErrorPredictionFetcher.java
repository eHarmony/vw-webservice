/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.predictionfetcher;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.eharmony.matching.vw.webservice.core.vwprediction.Prediction;
import com.eharmony.matching.vw.webservice.core.vwprediction.StringPrediction;

/**
 * @author vrahimtoola Returns a single StringPrediction describing the error
 *         that occurred.
 */
public class ErrorPredictionFetcher implements PredictionFetcher {

	private final String errorMessage;

	public ErrorPredictionFetcher(String errorMessage) {
		checkArgument(StringUtils.isBlank(errorMessage) == false);

		this.errorMessage = errorMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetcher
	 * #fetchPredictions()
	 */
	@Override
	public Iterable<Prediction> fetchPredictions() {
		return new Iterable<Prediction>() {
			@Override
			public Iterator<Prediction> iterator() {
				return new SimpleIterator(errorMessage);
			}
		};
	}

	private static class SimpleIterator implements Iterator<Prediction> {
		private boolean hasNext = true;
		private StringPrediction nextPrediction;

		public SimpleIterator(String errorMessage) {
			nextPrediction = new StringPrediction(errorMessage);
		}

		@Override
		public boolean hasNext() {

			boolean toReturn = hasNext;
			hasNext = false;
			return toReturn;
		}

		@Override
		public Prediction next() {
			Prediction toReturn = nextPrediction;
			nextPrediction = null;
			return toReturn;
		}

		@Override
		public void remove() {

			throw new UnsupportedOperationException(
					"The 'remove' operation is not supported!");

		}

	}
}
