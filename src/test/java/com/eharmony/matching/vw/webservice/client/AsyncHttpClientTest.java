/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.JsonTestUtils;
import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample;
import com.google.gson.stream.JsonWriter;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.BodyGenerator;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.generators.InputStreamBodyGenerator;

/**
 * @author vrahimtoola
 *         Uses the Async Http Client to hit the web service. This is the only
 *         java client that I've been able to get to work!
 */
public class AsyncHttpClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientTest.class);

	private int roundsOfDataToSubmit = 1;

	private boolean testFailed = false;

	@Before
	public void setUp() {
		roundsOfDataToSubmit = 3; //this means 2 * (number of examples in ner.train) examples will be submitted to the web service.
		testFailed = false;
	}

	private synchronized void onTestFailed() {
		testFailed = true;
	}

	private synchronized boolean getTestFailed() {
		return testFailed;
	}

	/*
	 * The ignore annotation is to keep the travis-ci build from failing.
	 */
	@Ignore
	@Test
	public void plainTextExamplesTest() throws IOException, InterruptedException, ExecutionException {

		RequestBuilder builder = new RequestBuilder("POST");

		//note: assumes that a vw-webservice is running on localhost at 8080.
		//modify the address accordingly if it's running on a different host/port.

		Request request = builder.setUrl("http://localhost:8080/vw-webservice/predict/main").addHeader("Content-Type", ExampleMediaTypes.PLAINTEXT_1_0).setBody(getPlainTextInputStreamBodyGenerator()).build();

		doTest(request);
	}

	/*
	 * The ignore annotation is to keep the travis-ci build from failing.
	 */
	@Ignore
	@Test
	public void structuredJsonExamplesTest() throws IOException, InterruptedException, ExecutionException {

		RequestBuilder builder = new RequestBuilder("POST");

		//note: assumes that a vw-webservice is running on localhost at 8080.
		//modify the address accordingly if it's running on a different host/port.

		Request request = builder.setUrl("http://localhost:8080/vw-webservice/predict/main").addHeader("Content-Type", ExampleMediaTypes.STRUCTURED_JSON_1_0).setBody(getJsonInputStreamBodyGenerator()).build();

		doTest(request);
	}

	/*
	 * The main method that carries out the test agains the web service and
	 * verifies the results.
	 */
	private void doTest(Request request) throws InterruptedException, ExecutionException, IOException {
		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

		AsyncHandler<Response> asyncHandler = new AsyncHandler<Response>() {
			private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

			@Override
			public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
				content.writeTo(pipedOutputStream);
				return STATE.CONTINUE;
			}

			@Override
			public STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
				builder.accumulate(status);
				return STATE.CONTINUE;
			}

			@Override
			public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
				builder.accumulate(headers);
				return STATE.CONTINUE;
			}

			@Override
			public Response onCompleted() throws Exception {

				LOGGER.info("On complete called!");

				pipedOutputStream.flush();
				pipedOutputStream.close();

				return builder.build();

			}

			@Override
			public void onThrowable(Throwable arg0) {
				// TODO Auto-generated method stub
				LOGGER.error("Error: {}", arg0);
				onTestFailed();
			}

		};

		Future<Void> readingThreadFuture = Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				BufferedReader reader = new BufferedReader(new InputStreamReader(pipedInputStream));

				String readPrediction;

				int numPredictionsRead = 0;

				while ((readPrediction = reader.readLine()) != null) {
					//LOGGER.info("Got prediction: {}", readPrediction);
					numPredictionsRead++;
				}

				LOGGER.info("Read a total of {} predictions", numPredictionsRead);
				Assert.assertEquals(roundsOfDataToSubmit * 272274, numPredictionsRead);

				return null;
			}
		});

		Builder config = new AsyncHttpClientConfig.Builder();

		config.setRequestTimeoutInMs(-1); //need to set this to -1, to indicate wait forever. setting to 0 actually means a 0 ms timeout!

		AsyncHttpClient client = new AsyncHttpClient(config.build());

		client.executeRequest(request, asyncHandler).get();

		readingThreadFuture.get(); //verify no exceptions occurred when reading predictions

		client.close();

		Assert.assertFalse(getTestFailed());
	}

	/*
	 * Returns a body generator that places plain text examples into the request
	 * body.
	 */
	private BodyGenerator getPlainTextInputStreamBodyGenerator() throws IOException {

		//the examples
		//final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

		PipedInputStream pipedInputStream = new PipedInputStream();

		final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

		Executors.newCachedThreadPool().submit(new Runnable() {

			@Override
			public void run() {

				try {

					int submitRound = 0;

					for (int x = 0; x < roundsOfDataToSubmit; x++) {

						Iterable<StructuredExample> structuredExamplesIterable = TestUtils.getStructuredExamplesFromNerTrain();

						for (StructuredExample structuredExample : structuredExamplesIterable) {
							pipedOutputStream.write((structuredExample.getVWStringRepresentation() + "\n").getBytes());
							pipedOutputStream.flush();
						}

						LOGGER.info("Submitted round {} of examples...", ++submitRound);
					}

				}
				catch (Exception e) {
					LOGGER.error("Error in submitting examples to piped output stream!", e);
					onTestFailed();
				}
				finally {
					try {
						pipedOutputStream.close();
					}
					catch (IOException e) {
						LOGGER.error("Failed to close piped outputstream!", e);
						onTestFailed();
					}
				}

			}
		});

		return new InputStreamBodyGenerator(pipedInputStream);
	}

	/*
	 * Returns a body generator that places JSON formatted examples into the
	 * request body.
	 */
	private BodyGenerator getJsonInputStreamBodyGenerator() throws IOException {

		//the examples
		//final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

		PipedInputStream pipedInputStream = new PipedInputStream();

		final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

		Executors.newCachedThreadPool().submit(new Runnable() {

			@Override
			public void run() {

				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pipedOutputStream);
				JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);

				try {

					int submitRound = 0;

					jsonWriter.beginArray();

					for (int x = 0; x < roundsOfDataToSubmit; x++) {

						Iterable<StructuredExample> structuredExamplesIterable = TestUtils.getStructuredExamplesFromNerTrain();

						for (StructuredExample structuredExample : structuredExamplesIterable) {
							JsonTestUtils.writeExample(jsonWriter, structuredExample);
							outputStreamWriter.flush();
							pipedOutputStream.flush();
						}

						LOGGER.info("Submitted round {} of examples...", ++submitRound);
					}

					jsonWriter.endArray();

				}
				catch (Exception e) {
					LOGGER.error("Error in submitting examples to piped output stream!", e);
					onTestFailed();
				}
				finally {
					try {
						jsonWriter.flush();
					}
					catch (IOException e) {
						LOGGER.error("Error flushing json writer!", e);
						onTestFailed();
					}

					try {
						jsonWriter.close();
					}
					catch (IOException e) {
						LOGGER.error("Error closing json writer!", e);
						onTestFailed();
					}

					try {
						pipedOutputStream.close();
					}
					catch (IOException e) {
						LOGGER.error("Error closing piped outputstream!", e);
						onTestFailed();
					}
				}

			}
		});

		return new InputStreamBodyGenerator(pipedInputStream);
	}

}
