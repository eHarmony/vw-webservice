/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
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
 *         Uses the Async Http Client to hit the web service.
 */
public class AsyncHttpClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHttpClientTest.class);

	//@Ignore
	@Test
	public void simpleTest() throws IOException, InterruptedException, ExecutionException {

		RequestBuilder builder = new RequestBuilder("POST");

		Request request = builder.setUrl("http://localhost:8080/vw-webservice/predict/main").addHeader("Content-Type", ExampleMediaTypes.PLAINTEXT_1_0).setBody(getInputStreamBodyGenerator()).build();

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

				LOGGER.debug("Got {} predictions", numPredictionsRead);

				return null;
			}
		});

		Builder config = new AsyncHttpClientConfig.Builder();

		config.setRequestTimeoutInMs(-1);

		AsyncHttpClient client = new AsyncHttpClient(config.build());

		client.executeRequest(request, asyncHandler).get();

		readingThreadFuture.get();

		client.close();
	}

	private BodyGenerator getInputStreamBodyGenerator() throws IOException {

		//the examples
		//final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

		PipedInputStream pipedInputStream = new PipedInputStream();

		final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

		Executors.newCachedThreadPool().submit(new Runnable() {

			@Override
			public void run() {

				try {

					int submitRound = 0;

					for (int x = 0; x < 100; x++) {

						//the examples
						GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

						BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));

						String readExample;

						while ((readExample = reader.readLine()) != null) {
							pipedOutputStream.write((readExample + "\n").getBytes());
							pipedOutputStream.flush();
						}

						LOGGER.info("Submitted round {} of examples...", ++submitRound);
					}

					pipedOutputStream.close();
				}
				catch (Exception e) {
					LOGGER.error("Error in submitting examples to piped output stream!", e);
				}

			}
		});

		return new InputStreamBodyGenerator(pipedInputStream);
	}

}
