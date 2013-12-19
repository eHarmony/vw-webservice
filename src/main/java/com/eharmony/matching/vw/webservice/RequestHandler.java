/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionCompleteCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionExceptionCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitter;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFactory;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExamplesSubmittedCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchCompleteCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchExceptionCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionsIterable;
import com.eharmony.matching.vw.webservice.core.vwexample.Example;
import com.eharmony.matching.vw.webservice.core.vwprediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         Handles an individual request to submit examples to VW and read back
 *         the predictions.
 */
public class RequestHandler implements ExamplesSubmittedCallback,
		ExampleSubmissionCompleteCallback, ExampleSubmissionExceptionCallback,
		PredictionFetchCompleteCallback, PredictionFetchExceptionCallback {

	private final ExampleSubmitterFactory exampleSubmitterFactory;

	private final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(ExampleSubmitterFactory exampleSubmitterFactory) {

		checkNotNull(exampleSubmitterFactory);

		this.exampleSubmitterFactory = exampleSubmitterFactory;
	}

	public void handleRequest(ExamplesIterable examplesIterable,
			final AsyncResponse asyncResponse) {

		// get the example submitter
		ExampleSubmitter exampleSubmitter = exampleSubmitterFactory
				.getExampleSubmitter(examplesIterable);

	final PredictionsIterable predictionsIterable = exampleSubmitter
				.submitExamples(this, this, this, this, this);

		// TODO: determine if asyncResponse.resume should be called in this
		// thread or a separate thread.
		// for now calling this in a separate thread.
		new Thread(new Runnable() {

			@Override
			public void run() {

				asyncResponse.resume(new StreamingOutput() {

					@Override
					public void write(OutputStream output) throws IOException,
							WebApplicationException {

			for (Prediction prediction : predictionsIterable) {
							prediction.write(output);
						}

					}
				});
			}

		}).start();
	}

	@Override
	public void onPredictionFetchException(PredictionsIterable predictionFetcher,
			PredictionFetchException theException) {

		LOGGER.error("Prediction fetch exception: {}",
				theException.getMessage());

	}

	@Override
	public void onAllPredictionsFetched(PredictionsIterable predictionFetcher,
			BigInteger numPredictions) {

		LOGGER.info("All predictions fetched!");

	}

	@Override
	public void onExampleSubmissionException(ExampleSubmitter exampleSubmitter,
			ExampleSubmissionException theException) {

		LOGGER.warn("Example submission exception: {}",
				theException.getMessage());

	}

	@Override
	public void onAllExamplesSubmitted(ExampleSubmitter exampleSubmitter,
			BigInteger numberOfSubmittedExamples) {

		LOGGER.info("All examples submitted!");
	}

	@Override
	public void onExamplesSubmitted(ExampleSubmitter exampleSubmitter,
			Iterable<Example> theExamples) {

		for (Example e : theExamples) {
			LOGGER.info("Submitted example: {}", e.getVWStringRepresentation());
		}

	}
}
