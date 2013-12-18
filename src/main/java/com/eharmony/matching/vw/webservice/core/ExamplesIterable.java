/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola An iterable of examples to be submitted to VW. Also
 *         provides attributes that describe the type/quantity of examples.
 */
public interface ExamplesIterable extends Iterable<Example> {

	/*
	 * Returns attributes used to describe the set of examples.
	 */
	String getAttribute(String attributeKey);
}
