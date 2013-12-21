/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter.tcpip;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionCompleteCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionExceptionCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitter;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFeatures;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFeaturesImpl;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExamplesSubmittedCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.ErrorPredictionsIterable;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchCompleteCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchExceptionCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionsIterable;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.tcpip.TCPIPPredictionsIterable;
import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola An asynchronous, fail fast example submitter to submit
 *         examples to VW over a TCP IP socket.
 * 
 *         Making this package-private for now.
 */
class AsyncFailFastTCPIPExampleSubmitter implements ExampleSubmitter {

	private static final String NEWLINE = System.getProperty("line.separator");

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AsyncFailFastTCPIPExampleSubmitter.class);

	private final ExecutorService executorService;
	private final String vwHost;
	private final int vwPort;
	private final Iterable<Example> examples;

	public AsyncFailFastTCPIPExampleSubmitter(String vwHost, int vwPort,
			ExecutorService executorService, Iterable<Example> examples) {

		this.executorService = executorService;
		this.vwHost = vwHost;
		this.vwPort = vwPort;
		this.examples = examples;
	}

	@Override
	public PredictionsIterable submitExamples(
			final ExamplesSubmittedCallback submissionCallback,
			final ExampleSubmissionCompleteCallback submissionCompleteCallback,
			final ExampleSubmissionExceptionCallback exceptionCallback,
			PredictionFetchCompleteCallback predictionFetchCompleteCallback,
			PredictionFetchExceptionCallback predictionFetchExceptionCallback) {

		final ExampleSubmitter exampleSubmitter = this;

		try {
			final Socket socket = new Socket(vwHost, vwPort);

			executorService.submit(new Callable<Void>() {

				@Override
				public Void call() {

					OutputStream outputStream;

					List<Example> submittedExamples = new ArrayList<Example>();

					// TODO: consider if using a long here is safe.
					long numExamplesSubmitted = 0;

					try {

						outputStream = socket.getOutputStream();

						LOGGER.info("Starting to submit examples to VW...");

						BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(outputStream));

						for (Example example : examples) {

							// example.write(outputStream);
							writer.write(example.getVWStringRepresentation());
							writer.write(NEWLINE);

							numExamplesSubmitted++;

							if (submissionCallback != null) {

								submittedExamples.add(example);

								if (numExamplesSubmitted % 5 == 0) { //TODO: make this number (ie, 5) configurable
									submissionCallback
											.onExamplesSubmitted(
													exampleSubmitter,
													submittedExamples);

									submittedExamples.clear();
								}
							}
						}

						// fire off the event for any remainging submitted
						// examples
						if (submissionCallback != null
								&& submittedExamples.size() > 0)
						{
							submissionCallback.onExamplesSubmitted(
									exampleSubmitter, submittedExamples);
							
							submittedExamples.clear();
						}

						LOGGER.info("All examples submitted to VW!");

					} catch (Exception e) {

						LOGGER.error("Exception in VWExampleSubmitter: {}",
								e.getMessage(), e);

						if (exceptionCallback != null)
							exceptionCallback.onExampleSubmissionException(
									exampleSubmitter,
									new ExampleSubmissionException(e));

					} finally {

						try {
							if (socket != null)
								socket.shutdownOutput();
						} catch (IOException e2) {
							if (exceptionCallback != null)
								exceptionCallback.onExampleSubmissionException(
										exampleSubmitter,
										new ExampleSubmissionException(e2));
						}

						if (submissionCompleteCallback != null)
							submissionCompleteCallback.onAllExamplesSubmitted(
									exampleSubmitter,
									BigInteger.valueOf(numExamplesSubmitted));

					}

					return null;
				}

			});

			return new TCPIPPredictionsIterable(socket,
					predictionFetchCompleteCallback,
					predictionFetchExceptionCallback);

		} catch (Exception e1) {

			LOGGER.error("Exception communicating with VW: {}", e1.getMessage());
			return new ErrorPredictionsIterable(e1.getMessage());
		}

	}

    @Override
	public ExampleSubmitterFeatures getExampleSubmitterFeatures() {

	return new ExampleSubmitterFeaturesImpl(true, null);
	}


}
