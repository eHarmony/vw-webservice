/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.common.example.Example;
import com.eharmony.matching.vw.webservice.common.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.common.example.StringExample;
import com.eharmony.matching.vw.webservice.common.prediction.Prediction;
import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingEventHandler;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessingManager;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleSubmissionState;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchException;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.PredictionFetchState;

/**
 * @author vrahimtoola
 * 
 *         Tests the AsyncFailFastTCPIPExampleProcessor.
 */
public class AsyncFailFastTCPIPExampleProcessorTest implements ExampleProcessingEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncFailFastTCPIPExampleProcessorTest.class);

	/*
	 * These variables could get written to by the example submitting thread, so
	 * we need to make them volatile.
	 */
	private volatile boolean exampleReadExceptionThrown, exampleFormatExceptionThrown, exampleSubmissionExceptionThrown, exampleSubmissionCompleteCalled;
	private volatile boolean predictionFetchExceptionThrown, predictionFetchCompleteCalled;

	private ExampleSubmissionState expectedStateOnExampleSubmissionComplete;
	private PredictionFetchState expectedStateOnPredictionFetchComplete;
	private long expectedNumberOfSkippedExamples, expectedNumberOfSubmittedExamples;

	private CountDownLatch countDownLatch;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		exampleReadExceptionThrown = false;
		exampleFormatExceptionThrown = false;
		exampleSubmissionExceptionThrown = false;
		exampleSubmissionCompleteCalled = false;

		predictionFetchCompleteCalled = false;
		predictionFetchExceptionThrown = false;

		expectedNumberOfSkippedExamples = -1;
		expectedNumberOfSubmittedExamples = -1;

		expectedStateOnExampleSubmissionComplete = ExampleSubmissionState.Complete;
		expectedStateOnPredictionFetchComplete = PredictionFetchState.Complete;

		countDownLatch = new CountDownLatch(2);

	}

	/*
	 * Just a simple test to verify that examples can be submitted and read as
	 * expected.
	 */
	@Test(timeout = 10000)
	public void simpleTest() throws IOException, ExampleSubmissionException, InterruptedException {

		Iterable<Example> examples = getExamples("One", "Two", "Three");

		InputStream predictionInputStream = getPredictionInputStream("1", "2", "3");

		Socket socket = mock(Socket.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(outputStream);
		when(socket.getInputStream()).thenReturn(predictionInputStream);

		TCPIPSocketFactory socketFactory = mock(TCPIPSocketFactory.class);
		when(socketFactory.getSocket()).thenReturn(socket);

		AsyncFailFastTCPIPExampleProcessor toTest = new AsyncFailFastTCPIPExampleProcessor(socketFactory, Executors.newCachedThreadPool(), examples);

		expectedNumberOfSkippedExamples = 0;
		expectedNumberOfSubmittedExamples = 3;
		expectedStateOnExampleSubmissionComplete = ExampleSubmissionState.Complete;
		expectedStateOnPredictionFetchComplete = PredictionFetchState.Complete;

		Iterable<Prediction> predictions = toTest.submitExamples(this).getPredictionsIterable();

		int x = 0;

		for (Prediction p : predictions) {

			switch (x++) {

				case 0:
					Assert.assertEquals("1", p.getVWStringRepresentation());
					break;

				case 1:
					Assert.assertEquals("2", p.getVWStringRepresentation());
					break;

				case 2:
					Assert.assertEquals("3", p.getVWStringRepresentation());
					break;

				default:
					Assert.fail("Too many predictions!");
			}
		}

		Assert.assertEquals(3, x);

		boolean succeeded = countDownLatch.await(9, TimeUnit.SECONDS); //wait till the example thread is done as well.

		Assert.assertTrue("Waited for longer than 9 seconds!!", succeeded);

		//check that all examples got there
		BufferedReader bReader = new BufferedReader(new StringReader(new String(outputStream.toByteArray())));

		x = 0;
		String line = null;
		while ((line = bReader.readLine()) != null) {

			switch (x++) {

				case 0:
					Assert.assertEquals("One", line);
					break;

				case 1:
					Assert.assertEquals("Two", line);
					break;

				case 2:
					Assert.assertEquals("Three", line);
					break;

				default:
					Assert.fail("Too many examples!");
			}

		}

		Assert.assertEquals(3, x);

		verify(socketFactory, times(1)).getSocket();
		verify(socket, times(1)).getInputStream();
		verify(socket, times(1)).getOutputStream();
		verify(socket, times(1)).shutdownOutput();
		verify(socket, times(1)).close();

		//no exceptions should have been thrown
		Assert.assertFalse(exampleReadExceptionThrown);
		Assert.assertFalse(exampleFormatExceptionThrown);
		Assert.assertFalse(exampleSubmissionExceptionThrown);
		Assert.assertFalse(predictionFetchExceptionThrown);

		//the completion call backs should have been fired
		Assert.assertTrue(exampleSubmissionCompleteCalled);
		Assert.assertTrue(predictionFetchCompleteCalled);

	}

	/*
	 * Tests that an ExampleSubmissionException is thrown when the socket cannot
	 * be retrieved from the socket factory.
	 */
	@Test(expected = ExampleSubmissionException.class)
	public void throwsExampleSubmissionException() throws IOException, ExampleSubmissionException {

		Iterable<Example> examples = getExamples("One", "Two", "Three");

		TCPIPSocketFactory socketFactory = mock(TCPIPSocketFactory.class);
		when(socketFactory.getSocket()).thenThrow(UnknownHostException.class);

		AsyncFailFastTCPIPExampleProcessor toTest = new AsyncFailFastTCPIPExampleProcessor(socketFactory, Executors.newCachedThreadPool(), examples);

		toTest.submitExamples(this);

	}

	/*
	 * Tests that an ExampleReadException is handled as expected.
	 */
	@Test(timeout = 5000)
	public void handlesExampleReadException() throws IOException, ExampleSubmissionException, InterruptedException {

		Iterator iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true);
		when(iterator.next()).thenThrow(ExampleReadException.class);

		Iterable examples = mock(Iterable.class);
		when(examples.iterator()).thenReturn(iterator);

		InputStream predictionInputStream = getPredictionInputStream("1", "2", "3");

		Socket socket = mock(Socket.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(outputStream);
		when(socket.getInputStream()).thenReturn(predictionInputStream);

		TCPIPSocketFactory socketFactory = mock(TCPIPSocketFactory.class);
		when(socketFactory.getSocket()).thenReturn(socket);

		AsyncFailFastTCPIPExampleProcessor toTest = new AsyncFailFastTCPIPExampleProcessor(socketFactory, Executors.newCachedThreadPool(), examples);

		expectedNumberOfSkippedExamples = 0;
		expectedNumberOfSubmittedExamples = 0;
		expectedStateOnExampleSubmissionComplete = ExampleSubmissionState.ExampleReadFault;
		expectedStateOnPredictionFetchComplete = PredictionFetchState.Complete;

		Iterable<Prediction> predictions = toTest.submitExamples(this).getPredictionsIterable();

		int x = 0;

		for (Prediction p : predictions) {

			switch (x++) {

				case 0:
					Assert.assertEquals("1", p.getVWStringRepresentation());
					break;

				case 1:
					Assert.assertEquals("2", p.getVWStringRepresentation());
					break;

				case 2:
					Assert.assertEquals("3", p.getVWStringRepresentation());
					break;

				default:
					Assert.fail("Too many predictions!");
			}
		}

		Assert.assertEquals(3, x);

		countDownLatch.await(); //wait till example submission and prediction fetch are both done.

		verify(socketFactory, times(1)).getSocket();
		verify(socket, times(1)).getInputStream();
		verify(socket, times(1)).getOutputStream();
		verify(socket, times(1)).shutdownOutput();
		verify(socket, times(1)).close();

		Assert.assertTrue(exampleReadExceptionThrown);
		Assert.assertFalse(exampleFormatExceptionThrown);
		Assert.assertFalse(exampleSubmissionExceptionThrown);
		Assert.assertFalse(predictionFetchExceptionThrown);

		//the completion call backs should have been fired
		Assert.assertTrue(exampleSubmissionCompleteCalled);
		Assert.assertTrue(predictionFetchCompleteCalled);

	}

	/*
	 * Tests that example format exceptions are handled as expected.
	 */
	@Test(timeout = 5000)
	public void handlesExampleFormatException() throws IOException, ExampleSubmissionException, InterruptedException {

		StringExample errorExample = mock(StringExample.class);
		when(errorExample.getVWStringRepresentation()).thenThrow(ExampleFormatException.class);

		Iterator iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(new StringExample("One")).thenReturn(errorExample).thenReturn(new StringExample("Two"));

		Iterable examples = mock(Iterable.class);
		when(examples.iterator()).thenReturn(iterator);

		InputStream predictionInputStream = getPredictionInputStream("1", "2", "3");

		Socket socket = mock(Socket.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(outputStream);
		when(socket.getInputStream()).thenReturn(predictionInputStream);

		TCPIPSocketFactory socketFactory = mock(TCPIPSocketFactory.class);
		when(socketFactory.getSocket()).thenReturn(socket);

		AsyncFailFastTCPIPExampleProcessor toTest = new AsyncFailFastTCPIPExampleProcessor(socketFactory, Executors.newCachedThreadPool(), examples);

		expectedNumberOfSkippedExamples = 1;
		expectedNumberOfSubmittedExamples = 2;
		expectedStateOnExampleSubmissionComplete = ExampleSubmissionState.Complete;
		expectedStateOnPredictionFetchComplete = PredictionFetchState.Complete;

		Iterable<Prediction> predictions = toTest.submitExamples(this).getPredictionsIterable();

		int x = 0;

		for (Prediction p : predictions) {

			switch (x++) {

				case 0:
					Assert.assertEquals("1", p.getVWStringRepresentation());
					break;

				case 1:
					Assert.assertEquals("2", p.getVWStringRepresentation());
					break;

				case 2:
					Assert.assertEquals("3", p.getVWStringRepresentation());
					break;

				default:
					Assert.fail("Too many predictions!");
			}
		}

		Assert.assertEquals(3, x);

		countDownLatch.await(); //wait till example submission and prediction fetch are both done.

		//check that all examples got there
		BufferedReader bReader = new BufferedReader(new StringReader(new String(outputStream.toByteArray())));

		x = 0;
		String line = null;
		while ((line = bReader.readLine()) != null) {

			switch (x++) {

				case 0:
					Assert.assertEquals("One", line);
					break;

				case 1:
					Assert.assertEquals("Two", line);
					break;

				default:
					Assert.fail("Too many examples!");
			}

		}

		Assert.assertEquals(2, x);

		verify(socketFactory, times(1)).getSocket();
		verify(socket, times(1)).getInputStream();
		verify(socket, times(1)).getOutputStream();
		verify(socket, times(1)).shutdownOutput();
		verify(socket, times(1)).close();

		Assert.assertFalse(exampleReadExceptionThrown);
		Assert.assertTrue(exampleFormatExceptionThrown);
		Assert.assertFalse(exampleSubmissionExceptionThrown);
		Assert.assertFalse(predictionFetchExceptionThrown);

		//the completion call backs should have been fired
		Assert.assertTrue(exampleSubmissionCompleteCalled);
		Assert.assertTrue(predictionFetchCompleteCalled);

	}

	/*
	 * Tests that a prediction fetch exception is handled correctly.
	 */
	@Test(timeout = 5000)
	public void handlePredictionFetchException() throws IOException, ExampleSubmissionException, InterruptedException {

		Iterable<Example> examples = getExamples("One", "Two");

		Socket socket = mock(Socket.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(outputStream);

		InputStream inputStream = mock(InputStream.class, new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new IOException();
			}

		});

		when(socket.getInputStream()).thenReturn(inputStream);

		TCPIPSocketFactory socketFactory = mock(TCPIPSocketFactory.class);
		when(socketFactory.getSocket()).thenReturn(socket);

		AsyncFailFastTCPIPExampleProcessor toTest = new AsyncFailFastTCPIPExampleProcessor(socketFactory, Executors.newCachedThreadPool(), examples);

		expectedNumberOfSkippedExamples = 0;
		expectedNumberOfSubmittedExamples = 2;
		expectedStateOnExampleSubmissionComplete = ExampleSubmissionState.Complete;
		expectedStateOnPredictionFetchComplete = PredictionFetchState.PredictionFetchFault;

		Iterable<Prediction> predictions = toTest.submitExamples(this).getPredictionsIterable();

		int x = 0;

		for (Prediction p : predictions) {

			x++;
		}

		Assert.assertEquals(0, x);

		countDownLatch.await(); //wait till example submission and prediction fetch are both done.

		//check that all examples got there
		BufferedReader bReader = new BufferedReader(new StringReader(new String(outputStream.toByteArray())));

		x = 0;
		String line = null;
		while ((line = bReader.readLine()) != null) {

			switch (x++) {

				case 0:
					Assert.assertEquals("One", line);
					break;

				case 1:
					Assert.assertEquals("Two", line);
					break;

				default:
					Assert.fail("Too many examples!");
			}

		}

		Assert.assertEquals(2, x);

		verify(socketFactory, times(1)).getSocket();
		verify(socket, times(1)).getInputStream();
		verify(socket, times(1)).getOutputStream();
		verify(socket, times(1)).shutdownOutput();
		verify(socket, times(1)).close();

		Assert.assertFalse(exampleReadExceptionThrown);
		Assert.assertFalse(exampleFormatExceptionThrown);
		Assert.assertFalse(exampleSubmissionExceptionThrown);
		Assert.assertTrue(predictionFetchExceptionThrown);

		//the completion call backs should have been fired
		Assert.assertTrue(exampleSubmissionCompleteCalled);
		Assert.assertTrue(predictionFetchCompleteCalled);

	}

	private Iterable<Example> getExamples(String... examples) {

		List<Example> toReturn = new ArrayList<Example>();

		for (String s : examples) {

			toReturn.add(new StringExample(s));
		}

		return toReturn;
	}

	private InputStream getPredictionInputStream(String... predictions) {

		String newLine = System.getProperty("line.separator");

		StringBuilder sbr = new StringBuilder();

		for (String s : predictions) {
			sbr.append(s);
			sbr.append(newLine);
		}

		return new ByteArrayInputStream(sbr.toString().getBytes());
	}

	@Override
	public void onExampleReadException(ExampleProcessingManager exampleProcessingManager, ExampleReadException theException) {

		Assert.assertTrue(exampleProcessingManager.getExampleSubmissionState() == ExampleSubmissionState.ExampleReadFault);

		exampleReadExceptionThrown = true;
	}

	@Override
	public void onExampleFormatException(ExampleProcessingManager exampleProcessingManager, ExampleFormatException theException) {

		//the async tcp ip example processor carries on when it encounters an example format exception,
		//and doesn't consider it to be a fault.
		Assert.assertTrue(exampleProcessingManager.getExampleSubmissionState() == ExampleSubmissionState.OnGoing);

		exampleFormatExceptionThrown = true;

	}

	@Override
	public void onExampleSubmissionException(ExampleProcessingManager exampleProcessingManager, ExampleSubmissionException theException) {

		Assert.assertTrue(exampleProcessingManager.getExampleSubmissionState() == ExampleSubmissionState.ExampleSubmissionFault);

		exampleSubmissionExceptionThrown = true;
	}

	@Override
	public void onExampleSubmissionComplete(ExampleProcessingManager exampleProcessingManager) {

		LOGGER.info("Example submission complete called!");

		Assert.assertTrue(exampleProcessingManager.getExampleSubmissionState() == expectedStateOnExampleSubmissionComplete);
		Assert.assertEquals(exampleProcessingManager.getTotalNumberOfExamplesSkipped(), expectedNumberOfSkippedExamples);
		Assert.assertEquals(exampleProcessingManager.getTotalNumberOfExamplesSubmitted(), expectedNumberOfSubmittedExamples);

		exampleSubmissionCompleteCalled = true;

		countDownLatch.countDown();

	}

	@Override
	public void onPredictionFetchException(ExampleProcessingManager exampleProcessingManager, PredictionFetchException theException) {

		Assert.assertTrue(exampleProcessingManager.getPredictionFetchState() == PredictionFetchState.PredictionFetchFault);

		predictionFetchExceptionThrown = true;
	}

	@Override
	public void onPredictionFetchComplete(ExampleProcessingManager exampleProcessingManager) {

		LOGGER.info("Prediction fetch complete called!");

		Assert.assertTrue(exampleProcessingManager.getPredictionFetchState() == expectedStateOnPredictionFetchComplete);

		predictionFetchCompleteCalled = true;

		countDownLatch.countDown();

	}

}
