/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.predictionfetcher.tcpip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchCompleteCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchExceptionCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetcher;
import com.eharmony.matching.vw.webservice.core.vwprediction.Prediction;
import com.eharmony.matching.vw.webservice.core.vwprediction.StringPrediction;

/**
 * @author vrahimtoola
 * 
 *         Reads predictions from VW over a TCP-IP socket.
 * 
 *         TODO: make it so that the example submitter doesn't start submitting
 *         examples until someone actually starts to read predictions. TODO:
 *         test what happens if the prediction fetcher closes the socket before
 *         all examples have been submitted.
 */
public class TCPIPPredictionFetcher implements PredictionFetcher,
		Iterable<Prediction>, Iterator<Prediction> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TCPIPPredictionFetcher.class);

	private final Socket socket;
	private final BufferedReader reader;
	private final PredictionFetchExceptionCallback predictionFetchExceptionCallback;
	private final PredictionFetchCompleteCallback predictionFetchCompleteCallback;

	private String nextLineToReturn = null;
	private long numTotalPredictions = 0;

	public TCPIPPredictionFetcher(Socket socket,
			PredictionFetchCompleteCallback predictionFetchCompleteCallback,
			PredictionFetchExceptionCallback predictionFetchExceptionCallback)
			throws IOException {

		this.reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		this.predictionFetchExceptionCallback = predictionFetchExceptionCallback;
		this.predictionFetchCompleteCallback = predictionFetchCompleteCallback;
		this.socket = socket;
	}

	@Override
	public Iterable<Prediction> fetchPredictions() {

		return this;
	}

	@Override
	public boolean hasNext() {
		return nextLineToReturn != null;
	}

	@Override
	public Prediction next() {
		String toReturn = nextLineToReturn;

		advance();

		return new StringPrediction(toReturn);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"The 'remove' operation is not supported!");
	}

	@Override
	public Iterator<Prediction> iterator() {
		advance();
		return this;
	}

	private void advance() {

		boolean closeReader = false;
		try {
			nextLineToReturn = reader.readLine();

			closeReader = nextLineToReturn == null;

			if (nextLineToReturn != null)
				numTotalPredictions++;

		} catch (Exception e) {
			LOGGER.error("Error in VWPredictionIterator: {}", e.getMessage(), e);
			closeReader = true;

			if (predictionFetchExceptionCallback != null)
				predictionFetchExceptionCallback.onPredictionFetchException(
						this, new PredictionFetchException(e));

		} finally {
			if (closeReader) {
				try {
					reader.close();
				} catch (Exception e2) {
					LOGGER.warn(
							"Failed to close the reader in VWPredictionIterator: {}",
							e2.getMessage(), e2);
				}

				if (socket.isClosed() == false)
					try {
						socket.close();
					} catch (Exception e2) {
						LOGGER.warn(
								"Failed to close the socket in VWPredictionIterator: {}",
								e2.getMessage(), e2);
					}

				if (predictionFetchCompleteCallback != null)
					predictionFetchCompleteCallback.onAllPredictionsFetched(
							this, BigInteger.valueOf(numTotalPredictions));
			}
		}
	}

}
