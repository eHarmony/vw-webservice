/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.vwexample;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola A VW example that's represented as a simple string, ie,
 *         without it being possible to access it's individual components
 *         separately.
 * 
 *         Note that since VW examples are meant to be plain text, I'm using the
 *         UTF8 charset. See the corresponding messagebodyreader.
 */
public class SimpleStringVWExample implements VWExample {

	private final String vwExampleString;

	private static final byte[] newlineBytes = System.getProperty(
			"line.separator").getBytes(Charsets.UTF_8);

	/*
	 * Constructs a VW example using the exact String representation of it.
	 * 
	 * @param theString The VW example. May be empty, but cannot be null.
	 */
	public SimpleStringVWExample(String theString) {
		checkNotNull(theString, "Null string provided as example!");
		vwExampleString = theString;
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
		outputStream.write(vwExampleString.getBytes(Charsets.UTF_8));
		outputStream.write(newlineBytes);
	}

}
