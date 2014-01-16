/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingManager;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessor;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFeatures;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFeaturesImpl;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionState;
import com.google.common.base.Charsets;

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

	public AsyncFailFastTCPIPExampleProcessor(TCPIPSocketFactory socketFactory, ExecutorService executorService, Iterable<Example> examples) {

		this.executorService = executorService;
		this.socketFactory = socketFactory;
		this.examples = examples;
	}

	@Override
	public ExampleProcessingManager submitExamples(final ExampleProcessingEventHandler callback) throws ExampleSubmissionException {

		try {
			final Socket socket = socketFactory.getSocket();

			final TCPIPExampleProcessingManager exampleProcessingManager = new TCPIPExampleProcessingManager(socket, callback);

			executorService.submit(new Callable<Void>() {

				@Override
				public Void call() {

					OutputStream outputStream;

					boolean faulted = false;

					boolean stoppedPrematurely = false;

					BufferedWriter writer = null;

					long numExamplesSent = 0;

					try {

						outputStream = socket.getOutputStream();

						LOGGER.info("Starting to submit examples to VW...");

						writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8));

						for (Example example : examples) {

							String toWrite = null;

							try {
								toWrite = example.getVWStringRepresentation();
								writer.write(toWrite);
								writer.newLine();

								numExamplesSent++;

								if (numExamplesSent == 1) LOGGER.debug("First example: {}", example);

								exampleProcessingManager.incrementNumberOfExamplesSubmitted();

								LOGGER.trace("Submitted example #{}: {}", numExamplesSent, toWrite);
							}
							catch (ExampleFormatException e) {

								exampleProcessingManager.incrementNumberOfExamplesSkipped();
								if (callback != null) callback.onExampleFormatException(exampleProcessingManager, e);

							}

							if (exampleProcessingManager.isStopped()) {
								LOGGER.warn("Example submission process was stopped for some reason!");
								stoppedPrematurely = true;
								break;
							}
						}

						if (!stoppedPrematurely) LOGGER.info("All examples submitted to VW!");

						LOGGER.info("Sent a total of {} examples to VW", numExamplesSent);

					}
					catch (ExampleReadException e) {

						exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.ExampleReadFault);

						if (callback != null) callback.onExampleReadException(exampleProcessingManager, e);

						LOGGER.error("ExampleReadException in ExampleSubmitter: {}", e.getMessage(), e);

						faulted = true;
					}
					catch (Exception e) {

						exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

						if (callback != null) callback.onExampleSubmissionException(exampleProcessingManager, new ExampleSubmissionException(e));

						LOGGER.error("Other Exception in ExampleSubmitter: {}", e.getMessage(), e);

						faulted = true;
					}
					finally {

						if (writer != null) try {
							writer.flush(); //make sure that anything buffered by the bufferedwriter is flushed to the underlying stream
						}
						catch (IOException e) {

							exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

							if (callback != null) callback.onExampleSubmissionException(exampleProcessingManager, new ExampleSubmissionException(e));

							LOGGER.error("IOException when closing example writer in ExampleProcessor: {}", e.getMessage(), e);

							faulted = true;
						}

						if (socket != null) try {

							socket.shutdownOutput();
						}
						catch (IOException e2) {

							exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.ExampleSubmissionFault);

							if (callback != null) callback.onExampleSubmissionException(exampleProcessingManager, new ExampleSubmissionException(e2));

							LOGGER.error("IOException when shutting down socket output in ExampleProcessor: {}", e2.getMessage(), e2);

							faulted = true;
						}

						if (faulted == false) {
							if (stoppedPrematurely == false)
								exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.Complete);
							else {
								exampleProcessingManager.setExampleSubmissionState(ExampleSubmissionState.Stopped);
							}
						}

						if (callback != null) callback.onExampleSubmissionComplete(exampleProcessingManager);

					}

					return null;
				}

			});

			return exampleProcessingManager;
		}
		catch (Exception e1) {

			LOGGER.error("Exception in submitExamples(): {}", e1.getMessage());

			throw new ExampleSubmissionException(e1);
		}

	}

	@Override
	public ExampleProcessorFeatures getExampleProcessorFeatures() {

		return new ExampleProcessorFeaturesImpl(true, null);
	}

}
