/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.vwexample;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vrahimtoola An example to be submitted to VW.
 */
public interface Example {

	/*
	 * Returns the example exactly as it will be submitted to VW, which expects
	 * plain text examples.
	 * 
	 * @returns The plain text VW representation of the example.
	 */
	String getVWStringRepresentation();

	/*
	 * Writes the example out to the provided outputstream. The implementation
	 * does NOT close the outputstream (ie, the caller owns it), but can call
	 * 'flush()' on it.
	 * 
	 * The implementation will write out a newline after writing out the VW
	 * example to the stream, as VW expects each example to be on its own line.
	 * 
	 * Note that empty examples (ie, empty lines) will still be sent to VW.
	 * 
	 * @param outputStream The output stream to write the example to.
	 */
	void write(OutputStream outputStream) throws IOException;
}
