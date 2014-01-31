/**
 * 
 */
package com.eharmony.matching.vw.webservice.util;

import junit.framework.Assert;

import org.junit.Test;

import com.eharmony.matching.vw.webservice.util.StringIterable;

/**
 * @author vrahimtoola Tests the StringIterable.
 */
public class StringIterableTest {

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnNullChunkOfTextTest() {
		new StringIterable(null);
	}

	@Test
	public void BasicNewLinesTest() {
		String theTextString = "Line 1\nLine 2\nLine 3";

		StringIterable stringIterable = new StringIterable(theTextString);

		int x = 0;
		for (String ln : stringIterable) {
			switch (x++) {
			case 0:
				Assert.assertEquals("Line 1", ln);
				break;
			case 1:
				Assert.assertEquals("Line 2", ln);
				break;
			case 2:
				Assert.assertEquals("Line 3", ln);
				break;
			default:
				Assert.fail();
			}

		}
	}
}
