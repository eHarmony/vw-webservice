/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter.tcpip;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionCompleteCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmissionExceptionCallback;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitter;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterOptions;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExamplesSubmittedCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.ErrorPredictionFetcher;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchCompleteCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetchExceptionCallback;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.PredictionFetcher;
import com.eharmony.matching.vw.webservice.core.predictionfetcher.tcpip.TCPIPPredictionFetcher;
import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola An asynchronous, fail fast example submitter to submit
 *         examples to VW over a TCP IP socket.
 * 
 *         Making this package-private for now.
 */
class AsyncFailFastTCPIPExampleSubmitter implements ExampleSubmitter {

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
	public PredictionFetcher submitExamples(
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

					long numExamplesSubmitted = 0;

					try {

						outputStream = socket.getOutputStream();

						LOGGER.info("Starting to submit examples to VW...");

						for (Example example : examples) {

							example.write(outputStream);

							submittedExamples.add(example);

							if (submissionCallback != null
									&& numExamplesSubmitted % 5 == 0) {
								submissionCallback.onExamplesSubmitted(
										exampleSubmitter, submittedExamples);

								submittedExamples.clear();
							}

							numExamplesSubmitted++;
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

			return new TCPIPPredictionFetcher(socket,
					predictionFetchCompleteCallback,
					predictionFetchExceptionCallback);

		} catch (Exception e1) {

			return new ErrorPredictionFetcher(e1.getMessage());
		}

	}

	@Override
	public EnumSet<ExampleSubmitterOptions> getExampleSubmissionOptions() {
		// TODO return the example submitter options in effect.
		return null;
	}

}
