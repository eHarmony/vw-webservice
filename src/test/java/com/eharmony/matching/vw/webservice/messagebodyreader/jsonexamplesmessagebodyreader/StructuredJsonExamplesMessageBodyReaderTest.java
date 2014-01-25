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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample.Namespace;
import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample.Namespace.Feature;
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
				return ExampleMediaTypes.STRUCTURED_JSON_1_0;
			}
		}));

		Assert.assertFalse(toTest.isReadable(ExamplesIterable.class, null, null, MediaType.TEXT_PLAIN_TYPE));
	}

	/*
	 * Tests that the readFrom method works as expected.
	 */
	@Test
	public void readFromTest() throws IOException, InterruptedException, TimeoutException, ExecutionException {
		final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader exampleReader = new BufferedReader(new InputStreamReader(gzipInputStream));
		final StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();
		final StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

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

		String readExample = null;
		StructuredExample lastComputedExample = null;
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(pipedOutputStream, Charsets.UTF_8);

		JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);

		jsonWriter.beginArray();

		while ((readExample = exampleReader.readLine()) != null) {

			if (lastComputedExample != null) {
				Assert.assertEquals(lastComputedExample.getVWStringRepresentation(), exampleExchanger.exchange(null, 2000, TimeUnit.MILLISECONDS).getVWStringRepresentation());
			}

			if (readExample.trim().length() == 0) {
				//just a line - empty example
				lastComputedExample = StructuredExample.EMPTY_EXAMPLE;
			}
			else {
				//locate the " | "
				int indexOfSpacePipeSpace = readExample.indexOf(" | ");

				Assert.assertTrue(indexOfSpacePipeSpace > 0);

				String[] labelAndAllFeatures = readExample.split(" \\| ");

				Assert.assertEquals(2, labelAndAllFeatures.length);

				exampleBuilder.setLabel(labelAndAllFeatures[0]);

				String allFeaturesString = labelAndAllFeatures[1];

				String[] individualFeatures = allFeaturesString.split(" ");

				for (String individualFeature : individualFeatures) {
					namespaceBuilder.addFeature(individualFeature);
				}

				exampleBuilder.addNamespace(namespaceBuilder.build());

				lastComputedExample = exampleBuilder.build();
			}

			String vwStringRep = lastComputedExample.getVWStringRepresentation();

			Assert.assertEquals(readExample, vwStringRep);

			namespaceBuilder.clear();
			exampleBuilder.clear();

			//LOGGER.trace("Writing example: {}", lastComputedExample.getVWStringRepresentation());

			if (lastComputedExample != StructuredExample.EMPTY_EXAMPLE)
				writeExample(jsonWriter, lastComputedExample);
			else {
				jsonWriter.beginObject();
				jsonWriter.endObject();
			}

			jsonWriter.flush();

		}//end while

		jsonWriter.endArray();

		jsonWriter.flush();

		jsonWriter.close();

		LOGGER.trace("Verifying final example...");

		//don't forget to verify the very last example!
		Assert.assertEquals(lastComputedExample.getVWStringRepresentation(), exampleExchanger.exchange(null, 2000, TimeUnit.MILLISECONDS).getVWStringRepresentation());

		Assert.assertEquals(272274, readingThreadFuture.get().intValue()); //assert that no exceptions where thrown.

	}

	private void writeExample(JsonWriter jsonWriter, StructuredExample structuredExample) throws IOException {

		jsonWriter.beginObject();

		String label = structuredExample.getLabel();

		//always write the label out, this is how a pipe example is distinguished from an empty example.
		jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_LABEL_PROPERTY);

		if (StringUtils.isBlank(label)) {
			jsonWriter.nullValue();
		}
		else {
			jsonWriter.value(label);
		}

		//for the tag and namespaces properties, only write them if they're non-null
		String tag = structuredExample.getTag();

		if (StringUtils.isBlank(tag) == false) jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_TAG_PROPERTY).value(tag);

		Iterable<Namespace> namespaces = structuredExample.getNamespaces();

		if (namespaces != null) {

			jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_NAMESPACES_PROPERTY);

			jsonWriter.beginArray();

			for (Namespace ns : namespaces) {
				writeNamespace(ns, jsonWriter);
			}

			jsonWriter.endArray();

		}

		jsonWriter.endObject(); //for the empty example, just write the "{}". 

	}

	private void writeNamespace(Namespace namespace, JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();

		String name = namespace.getName();
		Float scale = namespace.getScalingFactor();

		if (StringUtils.isBlank(name) == false) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_NAME_PROPERTY).value(name);
		}

		if (scale != null) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_SCALING_FACTOR_PROPERTY).value(scale);
		}

		Iterable<Feature> features = namespace.getFeatures();

		if (features != null) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_FEATURES_PROPERTY);

			jsonWriter.beginArray();

			for (Feature feature : features) {
				writeFeature(feature, jsonWriter);
			}

			jsonWriter.endArray();
		}

		jsonWriter.endObject();
	}

	private void writeFeature(Feature feature, JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();

		String name = feature.getName();
		Float value = feature.getValue();

		if (StringUtils.isBlank(name) == false) {
			jsonWriter.name(StructuredJsonPropertyNames.FEATURE_NAME_PROPERTY).value(name);
		}

		if (value != null) {
			jsonWriter.name(StructuredJsonPropertyNames.FEATURE_VALUE_PROPERTY).value(value);
		}

		jsonWriter.endObject();
	}
}
