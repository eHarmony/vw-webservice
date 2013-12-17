/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vrahimtoola An example to be submitted to VW.
 */
public interface VWExample {

	/*
	 * Writes the example out to the provided outputstream. The implementation
	 * does NOT close the outputstream (ie, the caller owns it), but can call
	 * 'flush()' on it.
	 * 
	 * The implementation will write out a newline after writing out the VW
	 * example to the stream.
	 * 
	 * @param outputStream The output stream to write the example to.
	 */
	void write(OutputStream outputStream) throws IOException;
}
