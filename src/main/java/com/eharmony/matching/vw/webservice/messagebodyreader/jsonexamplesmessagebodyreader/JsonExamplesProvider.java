/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.InputStream;
import java.util.Iterator;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.example.Example;

/**
 * @author vrahimtoola
 * 
 *         Provides VW examples represented as JSON.
 */
public interface JsonExamplesProvider {

	/*
	 * Allows the caller to consume JSON examples from an input stream.
	 * 
	 * @param inputStream The input stream to consume JSON examples from.
	 * 
	 * @returns An iterator that allows the caller to iterate over the examples.
	 */
	Iterator<Example> getExamplesFromStream(InputStream inputStream) throws ExampleReadException;
}
