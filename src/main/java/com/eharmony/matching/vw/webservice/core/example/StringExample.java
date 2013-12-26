/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.example;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author vrahimtoola
 * 
 *         A VW example that's represented as a simple string, ie, without it
 *         being possible to access it's individual components separately.
 * 
 */
public class StringExample implements Example {

	private final String vwExampleString;

	/*
	 * Constructs a VW example using the exact String representation of it.
	 * 
	 * @param theString The VW example. May be empty, but cannot be null.
	 */
	public StringExample(String theString) {
		checkNotNull(theString, "Null string provided as example!");
		vwExampleString = theString;
	}

	@Override
	public String getVWStringRepresentation() {
		return vwExampleString;
	}

	@Override
	public String toString() {
		return getVWStringRepresentation();
	}
}
