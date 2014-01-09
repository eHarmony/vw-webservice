/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.OutputStreamContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola Tests the web service using the Jetty HTTP Client.
 */
public class JettyHttpClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JettyHttpClientTest.class);

	private boolean submissionFailed = false;

	@Before
	public void setUp() throws Exception {

		submissionFailed = false;

	}

	/*
	 * A small test (with just a few 100 or so examples).
	 */
	@Ignore
	@Test
	public void smallTest() throws Exception {

		//final String vwWebService = "http://lp-prod1.dc1.eharmony.com:8080/vw-webservice/predict/main";

		final String vwWebService = "http://localhost:8080/vw-webservice/predict/main";

		HttpClient httpClient = new HttpClient();
		httpClient.start(); //must call start, otherwise weird exceptions get thrown!

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		OutputStreamContentProvider outputStreamContentProvider = new OutputStreamContentProvider();

		final OutputStream exampleOutputStream = outputStreamContentProvider.getOutputStream();

		final CountDownLatch okToStartReading = new CountDownLatch(1);
		final CountDownLatch okToStartWriting = new CountDownLatch(1);

		//the thread to submit examples
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					okToStartWriting.await();
				}
				catch (InterruptedException e1) {
					LOGGER.error("Error in submitting examples (waiting): {}", e1.getMessage(), e1);

					return;
				}

				okToStartReading.countDown();

				String example = null;

				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exampleOutputStream, Charsets.UTF_8));

				long numExamplesWritten = 0;

				try {

					while ((example = testReader.readLine()) != null) {

						writer.write(example);
						writer.newLine();

						numExamplesWritten++;

						if (numExamplesWritten == 90) break;
					}
				}
				catch (IOException e) {

					LOGGER.error("Error in submitting examples: {}", e.getMessage(), e);
					onSubmissionFailed();
				}
				finally {
					try {
						writer.flush();
					}
					catch (IOException e) {
						LOGGER.error("Error in submitting examples (flushing writer): {}", e.getMessage(), e);
						onSubmissionFailed();
					}

					try {
						writer.close();
					}
					catch (IOException e) {
						LOGGER.error("Error in submitting examples (closing writer): {}", e.getMessage(), e);
						onSubmissionFailed();
					}

					try {
						exampleOutputStream.close();
					}
					catch (IOException e) {
						LOGGER.error("Error in submitting examples (closing outputstream): {}", e.getMessage(), e);
						onSubmissionFailed();
					}

					LOGGER.debug("Wrote a total of {} examples", numExamplesWritten);
				}
			}
		}).start();

		InputStreamResponseListener listener = new InputStreamResponseListener();

		httpClient.newRequest("localhost", 8080).path("/vw-webservice/predict/main").method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "text/plain").content(outputStreamContentProvider).send(listener);

		okToStartWriting.countDown();
		okToStartReading.await();

		LOGGER.info("Waiting for response...");

		// Wait for the response headers to arrive
		Response response = listener.get(5, TimeUnit.SECONDS);

		LOGGER.info("Got response status: {}", response.getStatus());

		LOGGER.info("Dumping response headers...");

		for (HttpField field : response.getHeaders()) {
			LOGGER.debug("-{}:{}", field.getName(), field.getValue());
		}

		LOGGER.debug("Getting input stream...");

		BufferedReader reader = new BufferedReader(new InputStreamReader(listener.getInputStream()));

		String prediction = null;

		long numPredictions = 0;

		while ((prediction = reader.readLine()) != null) {
			numPredictions++;

			if (numPredictions % 1000 == 0) LOGGER.debug("Read prediction...{}", prediction);
		}

		Assert.assertFalse(submissionFailed);

	}

	private void onSubmissionFailed() {
		submissionFailed = true;
	}
}
