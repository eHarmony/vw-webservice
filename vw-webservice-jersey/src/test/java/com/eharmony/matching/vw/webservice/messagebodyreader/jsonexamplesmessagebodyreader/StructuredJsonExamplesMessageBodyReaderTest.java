/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.client.TestUtils;
import com.eharmony.matching.vw.webservice.common.example.Example;
import com.eharmony.matching.vw.webservice.common.example.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.common.example.StructuredExample;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;

/**
 * @author vrahimtoola
 * 
 */
public class StructuredJsonExamplesMessageBodyReaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StructuredJsonExamplesMessageBodyReaderTest.class);

	private StructuredJsonExamplesMessageBodyReader toTest;

	@Before
	public void setUp() {
		toTest = new StructuredJsonExamplesMessageBodyReader();
	}

	/*
	 * Tests that the readFrom method works as expected.
	 */
	@Test
	public void isReadableTest() {

		Assert.assertTrue(toTest.isReadable(ExamplesIterable.class, null, null, new MediaType() {
			@Override
			public String toString() {
				return ExampleMediaTypes.STRUCTURED_JSON_0_1_0;
			}
		}));

		Assert.assertFalse(toTest.isReadable(ExamplesIterable.class, null, null, MediaType.TEXT_PLAIN_TYPE));
	}

	@Test
	public void throwAwayTest() throws IOException {
		StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();
		StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		exampleBuilder.setLabel("34");
		exampleBuilder.setTag("someTag");

		namespaceBuilder.setName("one");
		namespaceBuilder.addFeature("a", 12.34f);
		namespaceBuilder.addFeature("b", 45.1f);

		StructuredExample.Namespace firstNamespace = namespaceBuilder.build();

		namespaceBuilder.clear();

		namespaceBuilder.setName("two");
		namespaceBuilder.setScalingFactor(34.3f);
		namespaceBuilder.addFeature("bah", 0.038293f);
		namespaceBuilder.addFeature("another", 3.4000f);
		namespaceBuilder.addFeature("andThis", 2.0f);

		StructuredExample.Namespace secondNamespace = namespaceBuilder.build();

		exampleBuilder.addNamespace(firstNamespace);
		exampleBuilder.addNamespace(secondNamespace);

		StringWriter stringWriter = new StringWriter();
		
		JsonWriter jsonWriter = new JsonWriter(stringWriter);
		
		JsonTestUtils.writeExample(jsonWriter, exampleBuilder.build());
		
		jsonWriter.flush();
		jsonWriter.close();
		
		LOGGER.debug("The JSON is: {}", stringWriter.toString());
	}
	
	/*
	 * Tests that the readFrom method works as expected.
	 */
	@Test
	public void readFromTest() throws IOException, InterruptedException, TimeoutException, ExecutionException {

		final CountDownLatch readThreadIsReadyLatch = new CountDownLatch(1);
		final Exchanger<Example> exampleExchanger = new Exchanger<Example>();

		final PipedInputStream pipedInputStream = new PipedInputStream(); //the reading thread will read from this stream
		final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream); //the submission thread will write to this stream

		ExecutorService executorService = Executors.newCachedThreadPool();

		//-------
		//this is the thread that will read the structured examples and compare
		//them to what was submitted by the submitting thread.
		Future<Integer> readingThreadFuture = executorService.submit(new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {

				readThreadIsReadyLatch.countDown(); //signal to the writing thread that this thread is ready.

				Iterable<Example> readStructuredExamples = toTest.readFrom(ExamplesIterable.class, null, null, null, null, pipedInputStream);

				int numExamplesRead = 0;

				LOGGER.trace("Starting to read examples...");

				for (Example readExample : readStructuredExamples) {
					//LOGGER.trace("Read example: {}", readExample.getVWStringRepresentation());

					exampleExchanger.exchange(readExample);
					numExamplesRead++;
				}

				return Integer.valueOf(numExamplesRead);
			}
		});

		readThreadIsReadyLatch.await();

		LOGGER.trace("Writing examples...");

		StructuredExample lastComputedExample = null;
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pipedOutputStream, Charsets.UTF_8);

		JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);

		jsonWriter.beginArray();

		Iterable<StructuredExample> structuredExamplesIterable = TestUtils.getStructuredExamplesFromNerTrain();

		for (StructuredExample example : structuredExamplesIterable) {

			if (lastComputedExample != null) {
				Assert.assertEquals(lastComputedExample.getVWStringRepresentation(), exampleExchanger.exchange(null, 2000, TimeUnit.MILLISECONDS).getVWStringRepresentation());
			}

			if (example != StructuredExample.EMPTY_EXAMPLE)
				JsonTestUtils.writeExample(jsonWriter, example);
			else {
				jsonWriter.beginObject();
				jsonWriter.endObject();
			}

			jsonWriter.flush();

			lastComputedExample = example;

		}//end for

		jsonWriter.endArray();

		jsonWriter.flush();

		jsonWriter.close();

		LOGGER.trace("Verifying final example...");

		//don't forget to verify the very last example!
		Assert.assertEquals(lastComputedExample.getVWStringRepresentation(), exampleExchanger.exchange(null, 2000, TimeUnit.MILLISECONDS).getVWStringRepresentation());

		Assert.assertEquals(272274, readingThreadFuture.get().intValue()); //assert that no exceptions where thrown.

	}

}
