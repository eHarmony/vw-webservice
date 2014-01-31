/**
 * 
 */
package com.eharmony.matching.vw.webservice.example;

import junit.framework.Assert;

import org.junit.Test;

import com.eharmony.matching.vw.webservice.common.example.StructuredExample;
import com.eharmony.matching.vw.webservice.common.example.StructuredExample.ExampleBuilder;

/**
 * @author vrahimtoola
 * 
 *         Tests the StructuredExample class.
 */
public class StructuredExampleTest {

	/*
	 * Tests that a pipe example gets built properly.
	 */
	@Test
	public void testPipeExampleCreation() {

		ExampleBuilder exampleBuilder = new ExampleBuilder();
		StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		Assert.assertTrue(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

		exampleBuilder.clear();
		namespaceBuilder.clear();

		exampleBuilder.setLabel("some label");

		Assert.assertFalse(exampleBuilder.build() == StructuredExample.EMPTY_EXAMPLE);
		Assert.assertFalse(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

		exampleBuilder.clear();
		namespaceBuilder.clear();

		StructuredExample.Namespace namespace = namespaceBuilder.build();

		exampleBuilder.addNamespace(namespace);

		Assert.assertTrue(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

		exampleBuilder.clear();
		namespaceBuilder.clear();

		//TODO: consider verifying that labels cannot contain spaces...?
		exampleBuilder.setLabel("some label");
		Assert.assertFalse(exampleBuilder.build() == StructuredExample.EMPTY_EXAMPLE);
		Assert.assertFalse(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

		exampleBuilder.clear();
		namespaceBuilder.clear();

		namespaceBuilder.setName("some-namespace-name");
		namespaceBuilder.setScalingFactor(1.0f);
		exampleBuilder.addNamespace(namespaceBuilder.build());

		Assert.assertFalse(exampleBuilder.build() == StructuredExample.EMPTY_EXAMPLE);
		Assert.assertFalse(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

		exampleBuilder.clear();
		namespaceBuilder.clear();

		namespaceBuilder.addFeature("someFeature", null);

		exampleBuilder.addNamespace(namespaceBuilder.build());

		Assert.assertFalse(exampleBuilder.build() == StructuredExample.EMPTY_EXAMPLE);
		Assert.assertFalse(exampleBuilder.build() == StructuredExample.PIPE_EXAMPLE);

	}

	/*
	 * Tests that an empty example returns the empty string when it's supposed
	 * to.
	 */
	@Test
	public void testEmptyExampleReturnsEmptyString() {
		Assert.assertEquals("", StructuredExample.EMPTY_EXAMPLE.getVWStringRepresentation());
	}

	/*
	 * Tests that a PIPE example returns the pipe character when it's supposed
	 * to.
	 */
	@Test
	public void testPipeExampleReturnsPipeString() {
		Assert.assertEquals(" |", StructuredExample.PIPE_EXAMPLE.getVWStringRepresentation());
	}

	/*
	 * Simple test to verify that basic example building works as expected.
	 */
	@Test
	public void simpleExampleBuildingTest() {

		final String expectedOutput = "34 |one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2";

		StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();
		StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		exampleBuilder.setLabel("34");

		namespaceBuilder.setName("one");
		namespaceBuilder.addFeature("a", 12.34f);
		namespaceBuilder.addFeature("b", 45.1f);

		StructuredExample.Namespace firstNamespace = namespaceBuilder.build();

		namespaceBuilder.clear();

		namespaceBuilder.setName("two");
		namespaceBuilder.setScalingFactor(34.3f);
		namespaceBuilder.addFeature("bah", 0.038293f);
		namespaceBuilder.addFeature("another", 3.4000f);
		namespaceBuilder.addFeature("andThis", 2.0f);

		StructuredExample.Namespace secondNamespace = namespaceBuilder.build();

		exampleBuilder.addNamespace(firstNamespace);
		exampleBuilder.addNamespace(secondNamespace);

		//System.out.println(exampleBuilder.build().getVWStringRepresentation());

		Assert.assertEquals(expectedOutput, exampleBuilder.build().getVWStringRepresentation());
	}

	/*
	 * Like the above, but tests the Tag feature as well (since it was added
	 * later).
	 */
	@Test
	public void simpleExampleBuildingTestWithTag() {

		String expectedOutput = "34 someTag|one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2";

		StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();
		StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		exampleBuilder.setLabel("34");
		exampleBuilder.setTag("someTag");

		namespaceBuilder.setName("one");
		namespaceBuilder.addFeature("a", 12.34f);
		namespaceBuilder.addFeature("b", 45.1f);

		StructuredExample.Namespace firstNamespace = namespaceBuilder.build();

		namespaceBuilder.clear();

		namespaceBuilder.setName("two");
		namespaceBuilder.setScalingFactor(34.3f);
		namespaceBuilder.addFeature("bah", 0.038293f);
		namespaceBuilder.addFeature("another", 3.4000f);
		namespaceBuilder.addFeature("andThis", 2.0f);

		StructuredExample.Namespace secondNamespace = namespaceBuilder.build();

		exampleBuilder.addNamespace(firstNamespace);
		exampleBuilder.addNamespace(secondNamespace);

		Assert.assertEquals(expectedOutput, exampleBuilder.build().getVWStringRepresentation());

		//-----
		exampleBuilder.setLabel(null); //clear out just the label, leaving everything else as is
		expectedOutput = "someTag|one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2";
		Assert.assertEquals(expectedOutput, exampleBuilder.build().getVWStringRepresentation());
		//-----

		//-----
		exampleBuilder.setTag(null); //clear out the tag as well, leaving just the namespace bit
		expectedOutput = "|one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2";
		Assert.assertEquals(expectedOutput, exampleBuilder.build().getVWStringRepresentation());
		//-----

		//-----
		exampleBuilder.setLabel("theLabel"); //set just the label
		expectedOutput = "theLabel |one a:12.34 b:45.1 |two:34.3 bah:0.038293 another:3.4 andThis:2";
		Assert.assertEquals(expectedOutput, exampleBuilder.build().getVWStringRepresentation());
		//-----
	}

}
