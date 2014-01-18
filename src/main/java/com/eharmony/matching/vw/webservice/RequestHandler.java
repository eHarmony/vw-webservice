/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.WebApplicationException;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingManager;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessor;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFactory;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.prediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         Handles an individual request to submit examples to VW and read back
 *         the predictions.
 */
class RequestHandler implements ExampleProcessingEventHandler {

	private final ExampleProcessorFactory exampleProcessorFactory;

	private final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

	private final ExecutorService executorService;

	public RequestHandler(ExecutorService executorService, ExampleProcessorFactory exampleProcessorFactory) {

		this.exampleProcessorFactory = exampleProcessorFactory;
		this.executorService = executorService;
	}

	public ChunkedOutput<String> handleRequest(ExamplesIterable examplesIterable) {

		ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String>(String.class);

		// get the example processor.
		ExampleProcessor exampleProcessor = exampleProcessorFactory.getExampleProcessor(examplesIterable);

		if (exampleProcessor.getExampleProcessorFeatures().isAsync() == false)
			submitSynchronously(exampleProcessor, chunkedOutput);
		else {
			submitAsynchronously(exampleProcessor, chunkedOutput);
		}

		return chunkedOutput;
	}

	private void submitSynchronously(final ExampleProcessor exampleProcessor, ChunkedOutput<String> chunkedOutput) {

		final ExampleProcessingEventHandler eventHandler = this;

		long numPredictionsWritten = 0;

		Iterable<Prediction> predictions = null;

		ExampleProcessingManager exampleProcessingManager = null;

		try {

			LOGGER.info("About to submit examples...");

			// note: depending on the example submitter in use,
			// the call to submitExamples could spawn off a separate
			// thread to submit examples to VW.
			exampleProcessingManager = exampleProcessor.submitExamples(eventHandler);

			predictions = exampleProcessingManager.getPredictionsIterable();

			for (Prediction p : predictions) {

				try {
					String toWrite = p.getVWStringRepresentation() + "\n";

					LOGGER.trace("Writing prediction: {}", toWrite);

					chunkedOutput.write(toWrite);

					numPredictionsWritten++;
				}
				catch (IOException e) {
					LOGGER.error("IOException when writing out prediction! Message: {}", e.getMessage(), e);
					throw new WebApplicationException(e); //nothing we can do if we can't send any data back to the client!
				}
			}

			LOGGER.info("Submitted a total of {} examples", exampleProcessingManager.getTotalNumberOfExamplesSubmitted());
			LOGGER.info("Skipped a total of {} examples", exampleProcessingManager.getTotalNumberOfExamplesSkipped());
			LOGGER.info("Read a total of {} predictions from VW", exampleProcessingManager.getTotalNumberOfPredictionsFetched());
			LOGGER.info("Wrote a total of {} predictions", numPredictionsWritten);
			LOGGER.info("Final example submission state: {}", exampleProcessingManager.getExampleSubmissionState());
			LOGGER.info("Final prediction fetch state: {}", exampleProcessingManager.getPredictionFetchState());

		}
		catch (ExampleSubmissionException e) {

			LOGGER.error("Exception when submitting examples! Message: {}", e.getMessage(), e);

			//output.write(("Exception when submitting examples! Message: " + e.getMessage()).getBytes());
		}
		catch (Exception e) {
			//if any other exception occurs, stop the example submission process.
			LOGGER.error("Other exception when reading predictions: {}", e.getMessage(), e);

			if (exampleProcessingManager != null) {
				LOGGER.info("Stopping example submission...");
				exampleProcessingManager.stopAll();
				LOGGER.info("Example submission stopped.");
			}
			else {
				LOGGER.warn("Example processing manager was null!");
			}
		}
		finally {
			try {
				chunkedOutput.close();
			}
			catch (Exception e2) {

				LOGGER.error("Exception when flushing output stream of predictions! Message: {}", e2.getMessage(), e2);
			}

		}

	}

	private void submitAsynchronously(final ExampleProcessor exampleSubmitter, final ChunkedOutput<String> chunkedOutput) {

		executorService.submit(new Runnable() {

			@Override
			public void run() {

				submitSynchronously(exampleSubmitter, chunkedOutput);

			}

		});

	}

	@Override
	public void onExampleReadException(ExampleProcessingManager exampleProcessingManager, ExampleReadException theException) {
		LOGGER.error("Example read exception: {}", theException.getMessage(), theException);
	}

	@Override
	public void onExampleFormatException(ExampleProcessingManager exampleProcessingManager, ExampleFormatException theException) {
		LOGGER.warn("Example format exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onExampleSubmissionException(ExampleProcessingManager exampleProcessingManager, ExampleSubmissionException theException) {
		LOGGER.error("Example submission exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onExampleSubmissionComplete(ExampleProcessingManager exampleProcessingManager) {
		LOGGER.info("Example submission complete!");

	}

	@Override
	public void onPredictionFetchException(ExampleProcessingManager exampleProcessingManager, PredictionFetchException theException) {
		LOGGER.error("Prediction fetch exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onPredictionFetchComplete(ExampleProcessingManager exampleProcessingManager) {
		LOGGER.info("Prediction fetch complete!");

	}
}
