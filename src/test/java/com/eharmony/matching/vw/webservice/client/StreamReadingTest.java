package com.eharmony.matching.vw.webservice.client;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamReadingTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamReadingTest.class);

	@Test
	public void test() throws UnknownHostException, IOException, InterruptedException {

		//		final CountDownLatch countDownLatch = new CountDownLatch(1);
		//
		//		final CountDownLatch countDownLatch2 = new CountDownLatch(1);
		//
		//		new Thread(new Runnable() {
		//
		//			@Override
		//			public void run() {
		//
		//				try {
		//					ServerSocket serverSocket = new ServerSocket(26542);
		//
		//					countDownLatch.countDown();
		//
		//					Socket connectionSocket = serverSocket.accept();
		//
		//					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
		//
		//					for (int x = 0; x < 100; x++)
		//						writer.write(Integer.toString(x)
		//								+ "adfadsfdfadfddfdf\n");
		//
		//					writer.flush();
		//
		//					connectionSocket.close();
		//
		//					countDownLatch2.countDown();
		//
		//					serverSocket.close();
		//
		//				}
		//				catch (Exception e) {
		//
		//					LOGGER.error("Exception! {}", e.getMessage(), e);
		//				}
		//
		//			}
		//
		//		}).start();
		//
		//		countDownLatch.await();
		//
		//		Socket readerSocket = new Socket("localhost", 26542);
		//
		//		InputStreamReader iReader = new InputStreamReader(readerSocket.getInputStream());
		//
		//		countDownLatch2.await();
		//
		//		String line = null;
		//
		//		int numLinesRead = 0;
		//
		//		while (iReader.read(cbuf)) {
		//
		//			LOGGER.info("Read: {}", line);
		//
		//			numLinesRead++;
		//
		//			Thread.sleep(1000);
		//		}
		//
		//		LOGGER.info("read a total of {} lines", numLinesRead);

	}

}
