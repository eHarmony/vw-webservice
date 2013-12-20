/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.eharmony.matching.vw.webservice.core.vwexample.Example;

/**
 * @author vrahimtoola An implementation of ExamplesIterable.
 */
public class ExamplesIterableImpl implements ExamplesIterable {

	private final Map<String, String> attributesMap;
	private final Iterator<Example> exampleIterator;
	private final int numberOfExamples;

	public ExamplesIterableImpl(int numberOfExamples,
			Map<String, String> theMapOfAttributes,
			Iterator<Example> exampleIterator) {
		checkNotNull(exampleIterator);

		this.numberOfExamples = numberOfExamples;

		if (theMapOfAttributes == null)
			attributesMap = new HashMap<String, String>();
		else {
			attributesMap = theMapOfAttributes;
		}

		this.exampleIterator = exampleIterator;
	}

	@Override
	public Iterator<Example> iterator() {
		return exampleIterator;
	}

	@Override
	public String getAttribute(String attributeKey) {
		return attributesMap.get(attributeKey);
	}

	@Override
	public int getNumberOfExamples() {
		return this.numberOfExamples;
	}

}
