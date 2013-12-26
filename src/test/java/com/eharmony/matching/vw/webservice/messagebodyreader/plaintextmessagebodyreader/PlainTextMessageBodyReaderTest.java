/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextmessagebodyreader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.messagebodyreader.plaintextmessagebodyreader.PlainTextMessageBodyReader;

/**
 * @author vrahimtoola Tests the PlainTextMessageBodyReader.
 */
public class PlainTextMessageBodyReaderTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PlainTextMessageBodyReaderTest.class);

	/*
	 * Simple test of examples.
	 */
	@Test
	public void spacesAtBeginningAndDifferentNewlinesTest()
			throws WebApplicationException, IOException {

		for (String newLineToUse : new String[] { "\n", "\r", "\r\n" }) {
			PlainTextMessageBodyReader toTest = new PlainTextMessageBodyReader();

			MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

			StringBuilder theExamples = new StringBuilder();
			theExamples.append("Example 1");
			theExamples.append(newLineToUse);
			theExamples.append("Example 2");
			theExamples.append(newLineToUse);
			theExamples.append("Example3 and 4 and 5");
			theExamples.append(newLineToUse);
			theExamples.append(newLineToUse);

			// note: data needs to be encoded using the correct char set, which
			// must match the mediatype.
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					theExamples.toString().getBytes(
							ReaderWriter.getCharset(mediaType)));

			// the readFrom method only looks at the mediatype and the input
			// stream, so other params can be null.
			Iterable<Example> theIterable = toTest.readFrom(null, null, null,
					mediaType, null, byteArrayInputStream);

			int x = 0;
			for (Example example : theIterable) {
				switch (x++) {
				case 0:
					Assert.assertEquals("Example 1",
							example.getVWStringRepresentation());
					break;

				case 1:
					Assert.assertEquals("Example 2",
							example.getVWStringRepresentation());
					break;

				case 2:
					Assert.assertEquals("Example3 and 4 and 5",
							example.getVWStringRepresentation());
					break;

				case 3:
					Assert.assertEquals("", example.getVWStringRepresentation());
					break;

				default:
					Assert.fail();
				}
			}

			Assert.assertEquals(4, x);

		}

	}

	/*
	 * Tests a massive number of examples using the ner.train.gz training set
	 * from the vowpal wabbit github repository. This training set was taken
	 * from the '/test/train-sets/' subfolder of the vowpal wabbit github repo
	 * (git@github.com:JohnLangford/vowpal_wabbit.git).
	 */
	@Test
	public void hugeFileTest() throws IOException {
		// pass the gzip inputstream to the string message body reader, and at
		// the same time, read from the file.
		// then compare the examples read in to verify that they match.

		// the input stream to read directly from the file
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass()
				.getClassLoader().getResourceAsStream("ner.train.gz"));

		BufferedReader testReader = new BufferedReader(new InputStreamReader(
				gzipInputStream));

		// the input stream that the PlainTextMessageBodyReader will
		// use.
		GZIPInputStream gzipInputStreamForTestSubject = new GZIPInputStream(
				this.getClass().getClassLoader()
						.getResourceAsStream("ner.train.gz"));

		PlainTextMessageBodyReader toTest = new PlainTextMessageBodyReader();

		MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

		Iterable<Example> theIterableOfExamples = toTest.readFrom(null, null,
				null, mediaType, null, gzipInputStreamForTestSubject);

		int numExamples = 0;

		boolean dumpExamples = false; // turn on to see some examples

		for (Example example : theIterableOfExamples) {
			String expectedExample = testReader.readLine();

			Assert.assertEquals(expectedExample,
					example.getVWStringRepresentation());

			numExamples++;

			if (dumpExamples && numExamples % 21 == 0) // print every 21st
														// example
			{

				// TODO: get a jenkin's build going, turn on code coverage +
				// findbugs
				// etc etc

				LOGGER.debug("expected example: {}", expectedExample);
				LOGGER.debug("read example    : {}", example);
				LOGGER.debug("");
			}

		}

		Assert.assertTrue(testReader.readLine() == null); // ensure all examples
															// read and
															// verified.
	}
}
