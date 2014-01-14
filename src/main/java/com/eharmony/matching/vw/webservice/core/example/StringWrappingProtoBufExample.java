/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.example;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author vrahimtoola
 * 
 *         A VW Example that's backed by a
 *         ProtoBufStringWrapperVWExample.StringWrapperExample.
 */
public class StringWrappingProtoBufExample implements Example {

	private final ProtoBufStringWrapperVWExample.StringWrapperExample stringWrapperExample;

	public StringWrappingProtoBufExample(ProtoBufStringWrapperVWExample.StringWrapperExample stringWrapperExample) {
		checkNotNull(stringWrapperExample);
		this.stringWrapperExample = stringWrapperExample;
	}

	@Override
	public String getVWStringRepresentation() {

		return stringWrapperExample.getExampleString();
	}

	@Override
	public String toString() {
		return getVWStringRepresentation();
	}

}
