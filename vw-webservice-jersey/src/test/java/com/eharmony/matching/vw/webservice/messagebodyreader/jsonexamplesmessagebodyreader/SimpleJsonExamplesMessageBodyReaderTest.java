/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.common.example.Example;
import com.eharmony.matching.vw.webservice.common.example.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;

/**
 * @author vrahimtoola
 * 
 *         Tests the SimpleJsonExamplesMessageBodyReader.
 */
public class SimpleJsonExamplesMessageBodyReaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJsonExamplesMessageBodyReaderTest.class);

	private SimpleJsonExamplesMessageBodyReader toTest;

	@Before
	public void setUp() {
		toTest = new SimpleJsonExamplesMessageBodyReader();
	}

	/*
	 * Tests that the readFrom method works as expected.
	 */
	@Test
	public void isReadableTest() {

		Assert.assertTrue(toTest.isReadable(ExamplesIterable.class, null, null, new MediaType() {
			@Override
			public String toString() {
				return ExampleMediaTypes.SIMPLE_JSON_1_0;
			}
		}));

		Assert.assertFalse(toTest.isReadable(ExamplesIterable.class, null, null, MediaType.TEXT_PLAIN_TYPE));
	}

	/*
	 * Tests that the readFrom method works as expected.
	 */
	@Test
	public void readFromTest() throws IOException, InterruptedException, TimeoutException, ExecutionException {
		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

		final CountDownLatch startWritingExamplesLatch = new CountDownLatch(1);

		final Exchanger<String> exampleExchanger = new Exchanger<String>();

		final JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(pipedOutputStream));

		Future<Void> successFuture = Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				startWritingExamplesLatch.await();

				String readExample = null;

				jsonWriter.beginArray();

				while ((readExample = testReader.readLine()) != null) {
					jsonWriter.beginObject();
					jsonWriter.name("example");
					jsonWriter.value(readExample);
					jsonWriter.endObject();

					jsonWriter.flush();
					pipedOutputStream.flush(); //need to flush right after writing, otherwise the reading thread won't get it 

					exampleExchanger.exchange(readExample, 1, TimeUnit.SECONDS); //don't care about what's being given to us in exchange
				}

				jsonWriter.endArray();
				jsonWriter.flush();
				jsonWriter.close();
				pipedOutputStream.flush();
				pipedOutputStream.close();

				return null;
			}

		});

		ExamplesIterable examplesIterable = toTest.readFrom(ExamplesIterable.class, null, null, null, null, pipedInputStream);

		startWritingExamplesLatch.countDown(); //tell the example writing thread to start writing examples

		long numExamplesProcessed = 0;

		for (Example example : examplesIterable) { //start reading the examples

			Assert.assertEquals(exampleExchanger.exchange("", 1, TimeUnit.SECONDS), example.getVWStringRepresentation());

			if (numExamplesProcessed % 20000 == 0) LOGGER.debug("Read example: {}", example.getVWStringRepresentation());

			numExamplesProcessed++;
		}

		successFuture.get(); //verify no exceptions were thrown.

		Assert.assertEquals(272274, numExamplesProcessed); //verify all examples in ner.train.gz were processed.	
	}

}
