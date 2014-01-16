package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.example.Example;
import com.google.common.base.Charsets;
import com.google.gson.stream.JsonWriter;

public class GsonJsonExamplesProviderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GsonJsonExamplesProviderTest.class);

	/*
	 * Tests that a whole bunch of examples can be written and read in JSON
	 * format.
	 */
	@Test
	public void test() throws IOException, InterruptedException, ExecutionException, TimeoutException {

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

		GsonJsonExamplesProvider toTest = new GsonJsonExamplesProvider();

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

		Iterator<Example> examplesIterable = toTest.getExamplesFromStream(pipedInputStream);

		startWritingExamplesLatch.countDown(); //tell the example writing thread to start writing examples

		long numExamplesProcessed = 0;

		while (examplesIterable.hasNext()) { //start reading the examples

			Example theExample = examplesIterable.next();

			Assert.assertEquals(exampleExchanger.exchange("", 1, TimeUnit.SECONDS), theExample.getVWStringRepresentation());

			if (numExamplesProcessed % 20000 == 0) LOGGER.debug("Read example: {}", theExample.getVWStringRepresentation());

			numExamplesProcessed++;
		}

		successFuture.get(); //verify no exceptions were thrown.

		Assert.assertEquals(272274, numExamplesProcessed); //verify all examples in ner.train.gz were processed.	

	}

}
