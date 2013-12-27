/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessor;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFeatures;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFeaturesImpl;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionState;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchState;
import com.eharmony.matching.vw.webservice.core.prediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         An asynchronous, fail fast example processor to submit examples to VW
 *         over a TCP IP socket.
 * 
 *         Making this package-private for now.
 */
class AsyncFailFastTCPIPExampleProcessor implements ExampleProcessor {

	private static final String NEWLINE = System.getProperty("line.separator");

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFailFastTCPIPExampleProcessor.class);

	private final ExecutorService executorService;
	private final TCPIPSocketFactory socketFactory;
	private final Iterable<Example> examples;

	private long numExamplesSubmitted, numExamplesSkipped;
	private ExampleSubmissionState exampleSubmissionState = ExampleSubmissionState.OnGoing;
	private TCPIPPredictionsIterator predictionsIterator;

	public AsyncFailFastTCPIPExampleProcessor(TCPIPSocketFactory socketFactory,
			ExecutorService executorService, Iterable<Example> examples) {

		this.executorService = executorService;
		this.socketFactory = socketFactory;
		this.examples = examples;
	}

	@Override
	public Iterable<Prediction> submitExamples(final ExampleProcessingEventHandler callback) throws ExampleSubmissionException {

		final ExampleProcessor exampleProcessor = this;

		try {
			final Socket socket = socketFactory.getSocket();

			final CountDownLatch countDownLatch = new CountDownLatch(1); //signal to the prediction fetcher.

			executorService.submit(new Callable<Void>() {

				@Override
				public Void call() {

					OutputStream outputStream;

					boolean faulted = false;

					BufferedWriter writer = null;

					boolean signaledPredictionFetcher = false;

					try {

						outputStream = socket.getOutputStream();

						LOGGER.info("Starting to submit examples to VW...");

						writer = new BufferedWriter(new OutputStreamWriter(outputStream));

						for (Example example : examples) {

							String toWrite = null;

							try {
								toWrite = example.getVWStringRepresentation();
								writer.write(toWrite);
								writer.write(NEWLINE);
								incrementNumberOfExamplesSubmitted();

								if (!signaledPredictionFetcher) {
									countDownLatch.countDown();
									signaledPredictionFetcher = true;
								}

								LOGGER.info("Submitted example: {}", toWrite);
							}
							catch (ExampleFormatException e) {

								incrementNumberOfExamplesSkipped();
								if (callback != null)
									callback.onExampleFormatException(exampleProcessor, e);

							}

						}

						LOGGER.info("All examples submitted to VW!");

					}
					catch (ExampleReadException e) {

						setExampleSubmissionState(ExampleSubmissionState.ExampleReadFault);

						if (callback != null)
							callback.onExampleReadException(exampleProcessor, e);

						LOGGER.error("ExampleReadException in VWExampleSubmitter: {}", e.getMessage(), e);

						faulted = true;
					}
					catch (Exception e) {

						setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

						if (callback != null)
							callback.onExampleSubmissionException(exampleProcessor, new ExampleSubmissionException(e));

						LOGGER.error("Other Exception in VWExampleSubmitter: {}", e.getMessage(), e);

						faulted = true;
					}
					finally {

						if (writer != null) try {
							writer.flush(); //make sure that anything buffered by the bufferedwriter is flushed to the underlying stream
						}
						catch (IOException e) {

							setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

							if (callback != null)
								callback.onExampleSubmissionException(exampleProcessor, new ExampleSubmissionException(e));

							LOGGER.error("IOException when closing example writer in ExampleProcessor: {}", e.getMessage(), e);

							faulted = true;
						}

						if (socket != null)
							try {

								socket.shutdownOutput();
							}
							catch (IOException e2) {

								setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

								if (callback != null)
									callback.onExampleSubmissionException(exampleProcessor, new ExampleSubmissionException(e2));

								LOGGER.error("IOException when shutting down socket output in ExampleProcessor: {}", e2.getMessage(), e2);

								faulted = true;
							}

						// if the prediction fetcher is still waiting on us, let it go.
						if (!signaledPredictionFetcher) {
							countDownLatch.countDown();
						}

						if (faulted == false)
							setExampleSubmissionState(ExampleSubmissionState.Complete);

						if (callback != null)
							callback.onExampleSubmissionComplete(exampleProcessor);

					}

					return null;
				}

			});

			final TCPIPPredictionsIterator theIterator = new TCPIPPredictionsIterator(exampleProcessor, socket, callback, countDownLatch);

			setPredictionsIterator(theIterator);

			return new Iterable<Prediction>() {

				@Override
				public Iterator<Prediction> iterator() {
					return theIterator;
				}
			};

		}
		catch (Exception e1) {

			LOGGER.error("Exception communicating with VW: {}", e1.getMessage());

			throw new ExampleSubmissionException(e1);
		}

	}

	@Override
	public ExampleProcessorFeatures getExampleSubmitterFeatures() {

		return new ExampleProcessorFeaturesImpl(true, null);
	}

	private synchronized void incrementNumberOfExamplesSubmitted() {
		numExamplesSubmitted++;
	}

	private synchronized void incrementNumberOfExamplesSkipped() {
		numExamplesSkipped++;
	}

	@Override
	public synchronized long getTotalNumberOfExamplesSubmitted() {
		return numExamplesSubmitted;
	}

	@Override
	public synchronized long getTotalNumberOfExamplesSkipped() {
		return numExamplesSkipped;
	}

	private synchronized void setExampleSubmissionState(ExampleSubmissionState newState) {
		exampleSubmissionState = newState;
	}

	@Override
	public synchronized ExampleSubmissionState getExampleSubmissionState() {
		return exampleSubmissionState;
	}

	private synchronized void setPredictionsIterator(TCPIPPredictionsIterator predictionsIterator) {
		this.predictionsIterator = predictionsIterator;
	}

	@Override
	public synchronized PredictionFetchState getPredictionFetchState() {

		if (predictionsIterator != null)
			return predictionsIterator.getPredictionFetchState();

		return PredictionFetchState.OnGoing;
	}
}
