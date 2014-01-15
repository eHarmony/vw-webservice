/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.protobufexamplesmessagebodyreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.ProtoBufStringWrapperVWExample;
import com.google.common.base.Charsets;

//import com.eharmony.matching.vw.webservice.core.ExamplesIterable;

/**
 * @author vrahimtoola Tests the ProtoBufExamplesMessageBodyReader.
 */
public class ProtoBufExamplesMessageBodyReaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtoBufExamplesMessageBodyReaderTest.class);

	private ProtoBufExamplesMessageBodyReader toTest;

	@Before
	public void setUp() {
		toTest = new ProtoBufExamplesMessageBodyReader();
	}

	/*
	 * Tests the isReadable method to verify correct behavior.
	 */
	@Test
	public void isReadableTest() {

		Assert.assertTrue(toTest.isReadable(ExamplesIterable.class, null, null, new MediaType() {
			@Override
			public String toString() {
				return ExampleMediaTypes.SIMPLE_PROTOBUF_1_0;
			}
		}));

		Assert.assertFalse(toTest.isReadable(ExamplesIterable.class, null, null, MediaType.TEXT_PLAIN_TYPE));
	}

	/*
	 * Tests that examples can be read correctly from the stream using the
	 * 'readFrom' method.
	 */
	@Test
	public void readFromTest() throws IOException, InterruptedException, ExecutionException, TimeoutException {

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

		final CountDownLatch startWritingExamplesLatch = new CountDownLatch(1);

		final Exchanger<String> exampleExchanger = new Exchanger<String>();

		Future<Void> successFuture = Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				startWritingExamplesLatch.await();

				String readExample = null;

				while ((readExample = testReader.readLine()) != null) {
					ProtoBufStringWrapperVWExample.StringWrapperExample writtenExample = ProtoBufStringWrapperVWExample.StringWrapperExample.newBuilder().setExampleString(readExample).build();

					writtenExample.writeDelimitedTo(pipedOutputStream);

					pipedOutputStream.flush(); //need to flush right after writing, otherwise the reading thread won't get it 

					exampleExchanger.exchange(readExample, 1, TimeUnit.SECONDS); //don't care about what's being given to us in exchange
				}

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
