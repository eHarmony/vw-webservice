/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.vwprediction;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola A VW prediction represented as a simple string (UTF8
 *         encoded).
 */
public class StringPrediction implements Prediction {

	private final String vwPrediction;

	private static final byte[] newlineBytes = System.getProperty(
			"line.separator").getBytes(Charsets.UTF_8);

	public StringPrediction(String theString) {
		checkNotNull(theString, "Null prediction provided!");
		vwPrediction = theString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.VWPrediction#write(java.io.
	 * OutputStream)
	 */
	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(vwPrediction.getBytes(Charsets.UTF_8));
		outputStream.write(newlineBytes);
	}

	@Override
	public String getVWStringRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
