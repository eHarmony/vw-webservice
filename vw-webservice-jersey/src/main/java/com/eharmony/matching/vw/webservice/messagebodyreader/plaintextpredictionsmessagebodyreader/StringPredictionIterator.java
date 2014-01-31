/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextpredictionsmessagebodyreader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.eharmony.matching.vw.webservice.core.prediction.Prediction;
import com.eharmony.matching.vw.webservice.core.prediction.StringPrediction;

/**
 * @author vrahimtoola
 * 
 *         Reads 1 string at a time from some input stream.
 * 
 *         TODO look at guava's abstract iterator and the test that comes with
 *         guava
 */
public class StringPredictionIterator implements Iterator<Prediction> {

	/*
	 * The reader.
	 */
	private final BufferedReader reader;

	/*
	 * The example to be returned, when 'next()' is called.
	 */
	private String nextPredictionToReturn = null;

	public StringPredictionIterator(InputStream inputStream, Charset charset)
			throws IOException {

		checkNotNull(inputStream, "A null input stream was provided!");
		reader = new BufferedReader(new InputStreamReader(inputStream, charset));
		advance();
	}

	@Override
	public boolean hasNext() {
		return nextPredictionToReturn != null;
	}

	@Override
	public Prediction next() {

		String toReturn = nextPredictionToReturn;

		if (toReturn == null)
			throw new NoSuchElementException("No element to return! Make sure to call 'hasNext()' and that it returns true before invoking this method!");

		try {
			advance();
		}
		catch (IOException e) {

			//TODO: consider throwing a custom exception here?
			throw new RuntimeException("Exception reading predictions! Message: "
					+ e.getMessage(), e);
		}

		return new StringPrediction(toReturn);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("The 'remove' operation is not supported!");
	}

	private void advance() throws IOException {

		nextPredictionToReturn = reader.readLine();
	}

}