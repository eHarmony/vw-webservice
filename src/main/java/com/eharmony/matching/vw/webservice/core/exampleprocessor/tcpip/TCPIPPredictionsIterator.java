/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessor;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchState;
import com.eharmony.matching.vw.webservice.core.prediction.Prediction;
import com.eharmony.matching.vw.webservice.core.prediction.StringPrediction;

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
class TCPIPPredictionsIterator implements Iterator<Prediction> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TCPIPPredictionsIterator.class);

	private final Socket socket;
	private final BufferedReader reader;
	private final ExampleProcessingEventHandler callback;
	private final ExampleProcessor exampleProcessor;

	private String nextLineToReturn = null;
	private PredictionFetchState predictionFetchState = PredictionFetchState.OnGoing;

	private boolean firstCallToHasNext = true;

	public TCPIPPredictionsIterator(ExampleProcessor exampleProcessor,
			Socket socket, ExampleProcessingEventHandler callback)
			throws IOException {

		this.exampleProcessor = exampleProcessor;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.callback = callback;
		this.socket = socket;
	}

	@Override
	public boolean hasNext() {

		if (firstCallToHasNext) {

			advance(); // don't want to call this in the constructor because
						// that could block.

			firstCallToHasNext = false;

		}

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
		throw new UnsupportedOperationException("The 'remove' operation is not supported!");
	}

	private void advance() {

		boolean closeReader = false;
		boolean faulted = false;
		try {

			nextLineToReturn = reader.readLine();

			LOGGER.info("Read prediction: {}", nextLineToReturn);

			closeReader = nextLineToReturn == null;

		}
		catch (Exception e) {

			LOGGER.error("Error in TCPIPPredictionIterator: {}", e.getMessage(), e);

			faulted = true;

			closeReader = true;

			setPredictionFetchState(PredictionFetchState.PredictionFetchFault);

			if (callback != null)
				callback.onPredictionFetchException(exampleProcessor, new PredictionFetchException(e));

		}
		finally {

			if (closeReader) {
				try {
					reader.close();
				}
				catch (Exception e2) {
					LOGGER.warn("Failed to close the reader in VWPredictionIterator: {}", e2.getMessage(), e2);
				}

				if (socket.isClosed() == false)
					try {
						socket.close();
					}
					catch (Exception e2) {
						LOGGER.warn("Failed to close the socket in VWPredictionIterator: {}", e2.getMessage(), e2);
					}

				nextLineToReturn = null; // need to set this explicitly, since
											// an exception may have
											// occurred
											// necessitating the closing of the
											// reader.

				if (!faulted)
					setPredictionFetchState(PredictionFetchState.Complete);

				if (callback != null)
					callback.onPredictionFetchComplete(exampleProcessor);
			}
		}
	}

	private synchronized void setPredictionFetchState(PredictionFetchState predictionFetchState) {
		this.predictionFetchState = predictionFetchState;
	}

	public synchronized PredictionFetchState getPredictionFetchState() {
		return predictionFetchState;
	}

}
