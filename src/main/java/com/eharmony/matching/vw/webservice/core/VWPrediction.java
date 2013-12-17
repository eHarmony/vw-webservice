/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vrahimtoola A prediction spit out by VW.
 */
public interface VWPrediction {

	/*
	 * Writes a prediction received from VW out to some output stream.
	 * 
	 * The implementation will write a newline after writing the prediction to
	 * the stream.
	 * 
	 * @param outputStream The stream to write the prediction to.
	 */
	void write(OutputStream outputStream) throws IOException;
}
