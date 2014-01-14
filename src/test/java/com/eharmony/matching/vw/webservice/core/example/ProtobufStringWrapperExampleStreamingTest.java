/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola Tests that streaming of the protobuf string wrapper vw
 *         example works fine.
 */
public class ProtobufStringWrapperExampleStreamingTest {

	/*
	 * Reads and writes a bunch of protobuf string wrapper examples to a stream
	 * and verifies that everything comes out a-ok.
	 */
	@Test
	public void basicTest() throws IOException {

		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		PipedOutputStream clientOutputStream = new PipedOutputStream();
		PipedInputStream clientInputStream = new PipedInputStream(clientOutputStream);

		String example = null;

		long numExamplesProcessed = 0;

		while ((example = testReader.readLine()) != null) {
			ProtoBufStringWrapperVWExample.StringWrapperExample writtenExample = ProtoBufStringWrapperVWExample.StringWrapperExample.newBuilder().setExampleString(example).build();

			writtenExample.writeDelimitedTo(clientOutputStream);

			ProtoBufStringWrapperVWExample.StringWrapperExample readExample = null;

			ProtoBufStringWrapperVWExample.StringWrapperExample.Builder readingBuilder = ProtoBufStringWrapperVWExample.StringWrapperExample.newBuilder();

			Assert.assertTrue(readingBuilder.mergeDelimitedFrom(clientInputStream));

			readExample = readingBuilder.build();

			Assert.assertEquals(example, readExample.getExampleString());

			numExamplesProcessed++;
		}

		clientInputStream.close();

		Assert.assertEquals(272274, numExamplesProcessed); //verify that we indeed wrote and read all examples in ner.train.gz
	}

}
