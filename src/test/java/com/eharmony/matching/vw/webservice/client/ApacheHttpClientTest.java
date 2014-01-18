package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Tests the web service using Apache's HTTP client.
 */
public class ApacheHttpClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientTest.class);

	@Ignore
	@Test
	public void simpleTest() throws SecurityException, IOException, InterruptedException {

		//java.util.logging used by HttpClient.
		//LogManager.getLogManager().readConfiguration(this.getClass().getClassLoader().getResourceAsStream("logging.properties"));

		//change these accordingly
		final String host = "localhost";
		final String hostAndPort = "http://" + host + ":8080/vw-webservice/predict/main";

		//the examples
		final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream));

		PipedOutputStream pipedOutputStream = new PipedOutputStream();
		BufferedWriter exampleWriter = new BufferedWriter(new OutputStreamWriter(pipedOutputStream));

		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
		InputStreamEntity inputStreamEntity = new InputStreamEntity(pipedInputStream, ContentType.TEXT_PLAIN);
		inputStreamEntity.setChunked(true); //chunked transfer encoding.

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(300000).setConnectTimeout(3000).build();

		final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

		final HttpPost postMessage = new HttpPost(hostAndPort);
		postMessage.setEntity(inputStreamEntity);
		postMessage.setHeader("Accept", "*/*");

		final CountDownLatch okToCloseHttpClientLatch = new CountDownLatch(1);

		new Thread(new Runnable() {

			@Override
			public void run() {

				LOGGER.debug("Invoking httpClient.execute...");

				try {
					httpClient.execute(postMessage, new ResponseHandler<String>() {

						@Override
						public String handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {

							boolean noExceptions = true;

							LOGGER.debug("Received response, headers are:");

							for (Header header : httpResponse.getAllHeaders()) {
								LOGGER.debug("--{}:{}", header.getName(), header.getValue());
							}

							LOGGER.debug("IsChunked: {}", httpResponse.getEntity().isChunked());
							LOGGER.debug("IsStreaming: {}", httpResponse.getEntity().isStreaming());

							try {
								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

								String lineString = null;

								long totalPredictions = 0;

								while ((lineString = bufferedReader.readLine()) != null) {
									//LOGGER.debug("Read prediction: {}", lineString);
									totalPredictions++;
								}

								LOGGER.debug("Read a total of {} predictions", totalPredictions);

							}
							catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								LOGGER.debug("IllegalStateException in response handler!!");
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

								LOGGER.debug("IOException in response handler!!");
							}

							okToCloseHttpClientLatch.countDown();
							return "";

						}
					});
				}
				catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					LOGGER.debug("ClientProtocolException when executing httpclient!!");
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					LOGGER.debug("IOException when executing httpclient!!");
				}

			}
		}).start();

		String exampleString = null;

		LOGGER.debug("Submitting examples...");

		long maxExamplesToWrite = 50000;
		long examplesWritten = 0;

		while ((exampleString = testReader.readLine()) != null) {
			exampleWriter.write(exampleString);
			exampleWriter.write("\n");

			examplesWritten++;

			if (examplesWritten == maxExamplesToWrite) break;
		}

		exampleWriter.flush();

		exampleWriter.close();

		postMessage.completed();

		LOGGER.debug("All examples written!");

		okToCloseHttpClientLatch.await();

		Thread.sleep(30000);

		httpClient.close();
	}
}
