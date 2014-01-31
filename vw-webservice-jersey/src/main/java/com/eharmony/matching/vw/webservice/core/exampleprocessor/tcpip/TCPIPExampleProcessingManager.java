/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingManager;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionState;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchState;
import com.eharmony.matching.vw.webservice.core.prediction.Prediction;

/**
 * @author vrahimtoola An implementation of ExampleProcessingManager for use by
 *         the AsyncFailFastTCPIPExampleProcessor.
 */
class TCPIPExampleProcessingManager implements ExampleProcessingManager {

	private long numExamplesSubmitted, numExamplesSkipped, numPredictionsFetched;
	private ExampleSubmissionState exampleSubmissionState = ExampleSubmissionState.OnGoing;
	private final TCPIPPredictionsIterator predictionsIterator;

	private boolean isStopped = false;

	public TCPIPExampleProcessingManager(Socket socket, ExampleProcessingEventHandler callback) throws IOException {
		this.predictionsIterator = new TCPIPPredictionsIterator(socket, callback, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#getPredictionsIterable()
	 */
	@Override
	public Iterable<Prediction> getPredictionsIterable() {

		return new Iterable<Prediction>() {

			@Override
			public Iterator<Prediction> iterator() {
				return predictionsIterator;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#stopAll()
	 */
	@Override
	public synchronized void stopAll() {

		isStopped = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#getTotalNumberOfExamplesSubmitted()
	 */
	@Override
	public synchronized long getTotalNumberOfExamplesSubmitted() {
		return numExamplesSubmitted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#getTotalNumberOfExamplesSkipped()
	 */
	@Override
	public synchronized long getTotalNumberOfExamplesSkipped() {
		return numExamplesSkipped;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#getExampleSubmissionState()
	 */
	@Override
	public synchronized ExampleSubmissionState getExampleSubmissionState() {
		return exampleSubmissionState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessingManager#getPredictionFetchState()
	 */
	@Override
	public PredictionFetchState getPredictionFetchState() {

		return predictionsIterator.getPredictionFetchState();
	}

	public synchronized void incrementNumberOfExamplesSubmitted() {
		numExamplesSubmitted++;
	}

	public synchronized void incrementNumberOfExamplesSkipped() {
		numExamplesSkipped++;
	}

	public synchronized void incrementNumberOfPredictionsFetched() {
		numPredictionsFetched++;
	}

	public synchronized void setExampleSubmissionState(ExampleSubmissionState newState) {
		exampleSubmissionState = newState;
	}

	public synchronized boolean isStopped() {
		return isStopped;
	}

	@Override
	public synchronized long getTotalNumberOfPredictionsFetched() {
		return numPredictionsFetched;
	}
}
