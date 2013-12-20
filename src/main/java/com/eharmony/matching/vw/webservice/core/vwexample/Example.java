/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.vwexample;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vrahimtoola An example to be submitted to VW, in it's proper input
 *         format.
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
	 * TODO this doesn't belong in here, an example doesn't have to be written
	 * out to an outputstream necessarily!! Writes the VW example (in the VW
	 * example format) out to the provided outputstream. The implementation does
	 * NOT close the outputstream (ie, the caller owns it), but can call
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
