/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextmessagebodyreader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.vwexample.Example;
import com.eharmony.matching.vw.webservice.core.vwexample.StringExample;

/**
 * @author vrahimtoola Reads 1 string at a time from some input stream.
 * 
 *         TODO look at guava's abstract iterator and the test that comes with
 *         guava
 */
public class StringExampleIterator implements Iterator<Example> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StringExampleIterator.class);

	/*
	 * The reader.
	 */
	private final BufferedReader reader;

	/*
	 * The example to be returned, when 'next()' is called.
	 */
	private String nextExampleToReturn = null;

	public StringExampleIterator(InputStream inputStream, Charset charset)
			throws IOException {

		checkNotNull(inputStream, "A null input stream was provided!");
		reader = new BufferedReader(new InputStreamReader(inputStream, charset));
		advance();
	}

	@Override
	public boolean hasNext() {
		return nextExampleToReturn != null;
	}

	@Override
	public Example next() {

		String toReturn = nextExampleToReturn;

		if (toReturn == null)
			throw new NoSuchElementException(
					"No element to return! Make sure to call 'hasNext()' and that it returns true before invoking this method!");

		try {
			advance();
		} catch (IOException e) {
			throw new RuntimeException("Exception reading examples! Message: "
					+ e.getMessage(), e);
		}

		return new StringExample(toReturn);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"The 'remove' operation is not supported!");
	}

	private void advance() throws IOException {

		nextExampleToReturn = reader.readLine();
	}

}