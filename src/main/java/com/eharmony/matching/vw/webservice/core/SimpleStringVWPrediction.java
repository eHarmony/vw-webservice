/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola A VW prediction represented as a simple string.
 */
public class SimpleStringVWPrediction implements VWPrediction {

	private final String vwPrediction;

	private final byte[] newlineBytes;

	private final Charset theCharset;

	public SimpleStringVWPrediction(String theString) {
		this(theString, Charsets.UTF_8);
	}

	public SimpleStringVWPrediction(String theString, Charset charset) {
		checkNotNull(theString);
		checkNotNull(charset);

		vwPrediction = theString;
		newlineBytes = System.getProperty("line.separator").getBytes(charset);
		theCharset = charset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.VWPrediction#write(java.io.
	 * OutputStream)
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(vwPrediction.getBytes(theCharset));
		outputStream.write(newlineBytes);
	}

}
