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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vrahimtoola Tests the web service using a plain old
 *         HttpUrlConnection.
 */
public class HttpUrlConnectionTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUrlConnectionTest.class);

	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	private final CountDownLatch allExamplesWritten = new CountDownLatch(1);

	private final CountDownLatch serviceStartedLatch = new CountDownLatch(1);

	@Ignore
	@Test
	public void test() throws MalformedURLException, IOException, InterruptedException, ExecutionException {

		startService();

		serviceStartedLatch.await();

		HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL("http://localhost:8081").openConnection());

		httpURLConnection.setChunkedStreamingMode(1024);
		httpURLConnection.setAllowUserInteraction(true);
		httpURLConnection.setConnectTimeout(0);
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setReadTimeout(0);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "text/plain");
		httpURLConnection.setRequestProperty("Accept", "*/*");
		httpURLConnection.setUseCaches(false);

		httpURLConnection.connect();

		BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

		java.util.concurrent.Future<Void> theFuture = startWriting(httpURLConnection);

		countDownLatch.await();

		String readPrediction;

		while ((readPrediction = reader.readLine()) != null) {
			LOGGER.debug("Read prediction: {}", readPrediction);
		}

		allExamplesWritten.await();

		//httpURLConnection.disconnect();

		theFuture.get();
	}

	private void startService() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(8081);

					serviceStartedLatch.countDown();

					Socket accepted = serverSocket.accept();

					BufferedReader reader = new BufferedReader(new InputStreamReader(accepted.getInputStream()));

					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream()));

					String read;

					while ((read = reader.readLine()) != null) {
						LOGGER.debug("Received: {}", read);
						writer.write("ok");
						writer.newLine();
						writer.flush();
					}

					writer.close();
				}
				catch (Exception e) {
					LOGGER.error("Error in server: {}", e.getMessage(), e);
				}

			}
		}).start();

	}

	private java.util.concurrent.Future<Void> startWriting(final HttpURLConnection httpURLConnection) {

		return Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				try {
					OutputStream outputStream = httpURLConnection.getOutputStream();

					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

					GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

					BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));

					String exampleToWrite;

					countDownLatch.countDown();

					while ((exampleToWrite = reader.readLine()) != null) {

						writer.write("hi");
						writer.write("\n");

						//LOGGER.debug("Wrote example: {}", exampleToWrite);

						//writer.flush();
						//outputStream.flush();
					}

					writer.flush();

					outputStream.flush();

					outputStream.close();

					allExamplesWritten.countDown();

					return null;
				}
				catch (Exception e) {
					LOGGER.error("Exception: {}", e.getMessage(), e);
				}
				return null;

			}
		});

	}

}
