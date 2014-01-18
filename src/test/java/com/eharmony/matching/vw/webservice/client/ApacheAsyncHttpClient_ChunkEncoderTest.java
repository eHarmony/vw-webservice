package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.ChunkEncoder;
import org.apache.http.impl.nio.reactor.SessionOutputBufferImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheAsyncHttpClient_ChunkEncoderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApacheAsyncHttpClient_ChunkEncoderTest.class);

	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	@Ignore
	@Test
	public void test() throws UnknownHostException, IOException, InterruptedException {

		startListening();

		countDownLatch.await();

		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 14728));

		Assert.assertNotNull(socketChannel);

		ChunkEncoder toTest = new ChunkEncoder(socketChannel, new SessionOutputBufferImpl(2048), new HttpTransportMetricsImpl());

		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream));

		String writtenExample;

		long numExamplesToWrite = -1;
		long numExamplesWritten = 0;

		while ((writtenExample = reader.readLine()) != null) {
			toTest.write(ByteBuffer.wrap(writtenExample.getBytes()));

			numExamplesWritten++;

			if (numExamplesWritten % 10000 == 0) LOGGER.debug("Wrote: {}", writtenExample);

			if (numExamplesWritten == numExamplesToWrite) break;
		}

		LOGGER.debug("Wrote a total of {} examples", numExamplesWritten);

		toTest.complete();

		socketChannel.close();
	}

	private void startListening() throws IOException {

		Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				ServerSocket serverSocket = new ServerSocket(14728);

				countDownLatch.countDown();

				Socket socket = serverSocket.accept();

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String readLine;

				long numLinesRead = 0;

				while ((readLine = reader.readLine()) != null) {
					//LOGGER.debug("Received: {}", readLine);

					numLinesRead++;
				}

				LOGGER.debug("Read a total of {} lines", numLinesRead);

				LOGGER.debug("reading done!");

				serverSocket.close();

				return null;

			}
		});

	}

}
