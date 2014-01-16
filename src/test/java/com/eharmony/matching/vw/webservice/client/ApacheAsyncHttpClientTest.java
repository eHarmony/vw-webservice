/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vrahimtoola
 * 
 *         Tests the web service using Apache's async HTTP client.
 */
public class ApacheAsyncHttpClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApacheAsyncHttpClientTest.class);

	@Ignore
	@Test
	public void simpleTest() throws IOException, InterruptedException, ExecutionException {

		//java.util.logging used by HttpClient.
		LogManager.getLogManager().readConfiguration(this.getClass().getClassLoader().getResourceAsStream("logging.properties"));

		//change these accordingly
		final String host = "localhost";
		final String hostAndPort = "http://" + host + ":8080/vw-webservice/predict/main";

		HttpHost httpHost = new HttpHost(host, 8080);

		//the examples
		final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		InputStreamEntity inputStreamEntity = new InputStreamEntity(gzipInputStream, ContentType.TEXT_PLAIN);
		inputStreamEntity.setChunked(true); //chunked transfer encoding.

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(300000).setConnectTimeout(3000).build();

		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();

		CountDownLatch countDownLatch = new CountDownLatch(2);

		try {
			httpclient.start();

			httpclient.execute(new MyHttpAsyncRequestProducer(httpHost, inputStreamEntity, countDownLatch), new MyHttpAsyncResponseConsumer(countDownLatch), new org.apache.http.concurrent.FutureCallback<HttpResponse>() {

				@Override
				public void cancelled() {
					LOGGER.debug("In future callback, cancel called");

				}

				@Override
				public void completed(HttpResponse arg0) {
					LOGGER.debug("In future callback, completed called");

				}

				@Override
				public void failed(Exception arg0) {
					LOGGER.debug("In future callback, failed called with exception message: {}", arg0.getMessage(), arg0);

				}
			});

		}
		catch (Exception e) {
			LOGGER.debug("Exception! Message: {}", e.getMessage(), e);

			Assert.fail();
		}
		finally {

			countDownLatch.await(5 * 60 * 1000, TimeUnit.MILLISECONDS);

			LOGGER.debug("Closing http client!");

			//Thread.sleep(5 * 60 * 1000);

			httpclient.close();
		}

	}

	private static class MyHttpAsyncRequestProducer implements HttpAsyncRequestProducer {

		private final HttpHost httpHost;
		private final InputStreamEntity inputStreamEntity;

		private boolean countdownLatchSignaled = false;

		private final CountDownLatch countDownLatch;

		private final long numTimesCalled = 0;

		public MyHttpAsyncRequestProducer(HttpHost httpHost, InputStreamEntity inputStreamEntity, CountDownLatch countDownLatch) {

			this.httpHost = httpHost;
			this.inputStreamEntity = inputStreamEntity;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void close() throws IOException {
			LOGGER.debug("Request close called");

			if (!countdownLatchSignaled) {
				countDownLatch.countDown();
				countdownLatchSignaled = true;
			}

		}

		@Override
		public void failed(Exception arg0) {
			LOGGER.debug("Request sending failed! Message: {}", arg0.getMessage(), arg0);

			if (!countdownLatchSignaled) {
				countDownLatch.countDown();
				countdownLatchSignaled = true;
			}
		}

		@Override
		public HttpRequest generateRequest() throws IOException, HttpException {

			LOGGER.debug("Generating request...");

			return new BasicHttpEntityEnclosingRequest("POST", "/vw-webservice/predict/main", new ProtocolVersion("HTTP", 1, 1)) {

				@Override
				public boolean expectContinue() {
					return false;
				}

				@Override
				public HttpEntity getEntity() {
					return inputStreamEntity;
				}
			};
		}

		@Override
		public HttpHost getTarget() {
			return httpHost;
		}

		@Override
		public boolean isRepeatable() {
			return false;
		}

		@Override
		public void produceContent(ContentEncoder contentEncoder, IOControl arg1) throws IOException {

			//			LOGGER.debug("Writing content of request with a content encoder of type {}...", contentEncoder.getClass());
			//
			//			LOGGER.debug("produce content called!");
			//
			//			contentEncoder.write(ByteBuffer.wrap("1 |tag ahd abdb".getBytes()));
			//			contentEncoder.write(ByteBuffer.wrap("\n".getBytes()));
			//
			//			contentEncoder.write(ByteBuffer.wrap("1 |tag ahd abdb".getBytes()));
			//			contentEncoder.write(ByteBuffer.wrap("\n".getBytes()));
			//
			//			contentEncoder.complete();

			LOGGER.debug("Writing content of request with a content encoder of type {}...", contentEncoder.getClass());

			final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

			BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));

			String line = null;

			//String newLine = "\r\n";

			long numExamples = 0;

			long numTotalBytes = 0;

			while ((line = reader.readLine()) != null) {

				//line = line.concat(newLine);

				contentEncoder.write(ByteBuffer.wrap(line.getBytes()));

				contentEncoder.write(ByteBuffer.wrap("\n".getBytes()));

				numExamples++;

				numTotalBytes += (line.getBytes().length + "\n".getBytes().length);

				if (numExamples == 1500) break;
			}

			contentEncoder.complete();

			LOGGER.debug("Wrote a total of {} examples and {} bytes", numExamples, numTotalBytes);

		}

		@Override
		public void requestCompleted(HttpContext arg0) {
			LOGGER.debug("Request completed!");

			if (!countdownLatchSignaled) {
				countDownLatch.countDown();
				countdownLatchSignaled = true;
			}
		}

		@Override
		public void resetRequest() throws IOException {
			LOGGER.debug("Reset request invoked!");

		}

	}

	private static class MyHttpAsyncResponseConsumer extends AbstractAsyncResponseConsumer<HttpResponse> {

		private HttpResponse httpResponse;

		private final CountDownLatch countDownLatch;

		public MyHttpAsyncResponseConsumer(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}

		@Override
		protected HttpResponse buildResult(HttpContext arg0) throws Exception {

			LOGGER.debug("Build result invoked!");

			return httpResponse;
		}

		@Override
		protected void onContentReceived(ContentDecoder contentDecoder, IOControl arg1) throws IOException {

			LOGGER.debug("OnContentReceived fired with a content decoder of {}", contentDecoder.getClass());

			//LOGGER.debug("Content received!");

			ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

			StringBuilder sbr = new StringBuilder();

			int numRead;

			while ((numRead = contentDecoder.read(byteBuffer)) > 0) {

				//LOGGER.debug("Read a total of {} bytes", numRead);

				CharBuffer charBuffer = byteBuffer.asCharBuffer();

				for (int x = 0; x < charBuffer.length(); x++)
					sbr.append(charBuffer.get(x));

				byteBuffer = ByteBuffer.allocate(2048);

			}

			//LOGGER.debug("Read: {}", sbr.toString());

			//LOGGER.debug("Is completed state: {}", arg0.isCompleted());

			if (contentDecoder.isCompleted()) {
				LOGGER.debug("All content received! contentDecoder's isComplete returned true");
				countDownLatch.countDown();
			}
		}

		@Override
		protected void onEntityEnclosed(HttpEntity arg0, ContentType arg1) throws IOException {
			LOGGER.debug("Entity enclosed, content type is: {}", arg1);
		}

		@Override
		protected void onResponseReceived(HttpResponse arg0) throws HttpException, IOException {
			LOGGER.debug("HTTP response received!");

			for (Header header : arg0.getAllHeaders()) {
				LOGGER.debug("{}:{}", header.getName(), header.getValue());
			}

		}

		@Override
		protected void releaseResources() {
			httpResponse = null;

		}

	}
}
