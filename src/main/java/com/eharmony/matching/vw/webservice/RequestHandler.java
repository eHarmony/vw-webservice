/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
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

	public RequestHandler(ExecutorService executorService,
			ExampleProcessorFactory exampleProcessorFactory) {

		this.exampleProcessorFactory = exampleProcessorFactory;
		this.executorService = executorService;
	}

	public void handleRequest(ExamplesIterable examplesIterable, final AsyncResponse asyncResponse) {

		// get the example submitter
		ExampleProcessor exampleProcessor = exampleProcessorFactory.getExampleProcessor(examplesIterable);

		if (exampleProcessor.getExampleSubmitterFeatures().isAsync() == false)
			submitSynchronously(exampleProcessor, asyncResponse);
		else {
			submitAsynchronously(exampleProcessor, asyncResponse);
		}

	}

	private void submitSynchronously(final ExampleProcessor exampleProcessor, AsyncResponse asyncResponse) {

		final ExampleProcessingEventHandler eventHandler = this;

		boolean resumedOk = asyncResponse.resume(new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {

				// note: depending on the example submitter in use,
				// the call to submitExamples could spawn off a separate
				// thread to submit examples to VW.
				Iterable<Prediction> predictions;
				try {

					LOGGER.info("About to submit examples...");

					predictions = exampleProcessor.submitExamples(eventHandler);

					for (Prediction p : predictions) {
						p.write(output);
					}

					LOGGER.info("Submitted a total of {} examples", exampleProcessor.getTotalNumberOfExamplesSubmitted());
					LOGGER.info("Skipped a total of {} examples", exampleProcessor.getTotalNumberOfExamplesSkipped());
					LOGGER.info("Final example submission state: {}", exampleProcessor.getExampleSubmissionState());
					LOGGER.info("Final prediction fetch state: {}", exampleProcessor.getPredictionFetchState());

				}
				catch (ExampleSubmissionException e) {

					LOGGER.error("Exception when submitting examples! Message: {}", e.getMessage(), e);

					output.write(("Exception when submitting examples! Message: " + e.getMessage()).getBytes());
				}

			}
		});

		if (!resumedOk)
			LOGGER.error("Failed to successfully resume sending data!");

	}

	private void submitAsynchronously(final ExampleProcessor exampleSubmitter, final AsyncResponse asyncResponse) {

		asyncResponse.setTimeoutHandler(new TimeoutHandler() {

			@Override
			public void handleTimeout(AsyncResponse asyncResponse) {

				LOGGER.error("Time out!!! This is bad...");

				asyncResponse.resume("");

			}
		});

		asyncResponse.register(new CompletionCallback() {

			@Override
			public void onComplete(Throwable throwable) {

				if (throwable != null)
					LOGGER.error("Exception thrown in completion callback!");

			}
		});

		asyncResponse.register(new ConnectionCallback() {

			@Override
			public void onDisconnect(AsyncResponse disconnected) {

				LOGGER.error("Client disconnected!!");

			}
		});

		executorService.submit(new Runnable() {

			@Override
			public void run() {

				submitSynchronously(exampleSubmitter, asyncResponse);

			}

		});

	}

	@Override
	public void onExampleReadException(ExampleProcessor processor, ExampleReadException theException) {

		LOGGER.error("Example read exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onExampleFormatException(ExampleProcessor processor, ExampleFormatException theException) {

		LOGGER.warn("Example format exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onExampleSubmissionException(ExampleProcessor processor, ExampleSubmissionException theException) {

		LOGGER.error("Example submission exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onExampleSubmissionComplete(ExampleProcessor processor) {

		LOGGER.info("Example submission complete!");

	}

	@Override
	public void onPredictionFetchException(ExampleProcessor processor, PredictionFetchException theException) {

		LOGGER.error("Prediction fetch exception: {}", theException.getMessage(), theException);

	}

	@Override
	public void onPredictionFetchComplete(ExampleProcessor processor) {

		LOGGER.info("Prediction fetch complete!");

	}
}
