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
 * @author vrahimtoola A VW example that's represented as a simple string, ie,
 *         without it being possible to access it's individual components
 *         separately.
 */
public class SimpleStringVWExample implements VWExample {

	private final String vwExampleString;

	private final byte[] newlineBytes;

	private final Charset theCharset;

	/*
	 * Constructs a VW example using the exact String representation of it.
	 * 
	 * @param theString The VW example. May be empty, but cannot be null.
	 */
	public SimpleStringVWExample(String theString) {
		this(theString, Charsets.UTF_8);
	}

	public SimpleStringVWExample(String theString, Charset charset) {
		checkNotNull(theString);
		checkNotNull(charset);

		vwExampleString = theString;
		newlineBytes = System.getProperty("line.separator").getBytes(charset);
		theCharset = charset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eharmony.matching.vw.webservice.core.VWExample#write(java.io.OutputStream
	 * )
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(vwExampleString.getBytes(theCharset));
		outputStream.write(newlineBytes);
	}

}
