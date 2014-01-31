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

import com.eharmony.matching.vw.webservice.common.prediction.Prediction;
import com.eharmony.matching.vw.webservice.common.prediction.StringPrediction;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchState;

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
	private final TCPIPExampleProcessingManager exampleProcessingManager;

	private String nextLineToReturn = null;
	private PredictionFetchState predictionFetchState = PredictionFetchState.OnGoing;

	private boolean firstCallToHasNext = true;

	public TCPIPPredictionsIterator(Socket socket, ExampleProcessingEventHandler callback, TCPIPExampleProcessingManager exampleProcessingManager) throws IOException {

		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.callback = callback;
		this.socket = socket;
		this.exampleProcessingManager = exampleProcessingManager;
	}

	public boolean hasNext() {

		if (firstCallToHasNext) {

			LOGGER.debug("First call to advance in TCP IP iterator!");

			advance(); // don't want to call this in the constructor because
						// that could block.

			firstCallToHasNext = false;

		}

		return nextLineToReturn != null;
	}

	public Prediction next() {
		String toReturn = nextLineToReturn;

		advance();

		return new StringPrediction(toReturn);
	}

	public void remove() {
		throw new UnsupportedOperationException("The 'remove' operation is not supported!");
	}

	private void advance() {

		boolean closeReader = false;
		boolean faulted = false;
		try {

			nextLineToReturn = reader.readLine();

			LOGGER.trace("Read prediction: {}", nextLineToReturn);

			closeReader = nextLineToReturn == null;

			if (nextLineToReturn != null) exampleProcessingManager.incrementNumberOfPredictionsFetched();

		}
		catch (Exception e) {

			LOGGER.error("Error in TCPIPPredictionIterator: {}", e.getMessage(), e);

			faulted = true;

			closeReader = true;

			setPredictionFetchState(PredictionFetchState.PredictionFetchFault);

			if (callback != null) callback.onPredictionFetchException(exampleProcessingManager, new PredictionFetchException(e));

		}
		finally {

			if (closeReader) {
				try {
					if (socket.isClosed() == false) reader.close();
				}
				catch (Exception e2) {
					LOGGER.warn("Failed to close the reader in predictions iterator: {}", e2.getMessage(), e2);
				}

				if (socket.isClosed() == false) try {
					socket.close();
				}
				catch (Exception e2) {
					LOGGER.warn("Failed to close the socket in predictions iterator: {}", e2.getMessage(), e2);
				}

				nextLineToReturn = null; // need to set this explicitly, since
											// an exception may have
											// occurred
											// necessitating the closing of the
											// reader.

				if (!faulted)
					setPredictionFetchState(PredictionFetchState.Complete);
				else {
					//faulted, so halt the example submission process
					LOGGER.warn("Stopping example submission from within the TCP IP predictions iterator...");
					exampleProcessingManager.stopAll();

					//if faulted, the prediction fetch state will already have been set in the exception handling code.
				}

				if (callback != null) callback.onPredictionFetchComplete(exampleProcessingManager);
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
