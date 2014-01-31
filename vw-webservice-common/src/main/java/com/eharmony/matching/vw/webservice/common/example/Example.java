/**
 * 
 */
package com.eharmony.matching.vw.webservice.common.example;

/**
 * @author vrahimtoola
 * 
 *         An example to be submitted to VW, in it's proper input format.
 */
public interface Example {

	/*
	 * Returns the example exactly as it will be submitted to VW, which expects
	 * plain text examples.
	 * 
	 * @throws ExampleFormatException to indicate that the format of the example
	 * isn't valid.
	 * 
	 * @returns The plain text VW representation of the example.
	 */
	String getVWStringRepresentation();

}
