/**
 * 
 */
package com.eharmony.matching.vw.webservice.common.prediction;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vrahimtoola
 * 
 *         A prediction spit out by VW.
 */
public interface Prediction {

	/*
	 * Returns the string representation of a Prediction. The returned
	 * representation should be convertible back to a Prediction. Intead of
	 * relying on the implementor providing a 'toString()' method that works
	 * sensibly, I thought it would be a better idea to t a proper
	 * implementation this way.
	 * 
	 * @returns The prediction, exactly as returned by VW.
	 */
	String getVWStringRepresentation();

	/*
	 * Writes a prediction received from VW out to some output stream.
	 * 
	 * The implementation will write a newline after writing the prediction to
	 * the stream.
	 * 
	 * @param outputStream The stream to write the prediction to. The caller
	 * owns this stream.
	 */
	void write(OutputStream outputStream) throws IOException;
}
