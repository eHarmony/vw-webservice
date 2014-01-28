/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import junit.framework.Assert;

import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample;
import com.google.common.collect.AbstractIterator;

/**
 * @author vrahimtoola
 *         General utility code for tests.
 */
public class TestUtils {

	/*
	 * Returns the examples from ner.train.gz as structured examples.
	 */
	public static Iterable<StructuredExample> getStructuredExamplesFromNerTrain() {

		return new Iterable<StructuredExample>() {

			@Override
			public Iterator<StructuredExample> iterator() {
				try {
					return getStructuredExampleIteratorFromNerTrain();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};

	}

	private static Iterator<StructuredExample> getStructuredExampleIteratorFromNerTrain() throws IOException {

		final GZIPInputStream gzipInputStream = new GZIPInputStream(TestUtils.class.getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader exampleReader = new BufferedReader(new InputStreamReader(gzipInputStream));
		final StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();
		final StructuredExample.Namespace.NamespaceBuilder namespaceBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		return new AbstractIterator<StructuredExample>() {

			@Override
			protected StructuredExample computeNext() {

				try {
					String readExample = exampleReader.readLine();

					exampleBuilder.clear();
					namespaceBuilder.clear();

					if (readExample != null) {
						if (readExample.trim().length() == 0) {
							//just a line - empty example
							return StructuredExample.EMPTY_EXAMPLE;
						}
						else {
							//locate the " | "
							int indexOfSpacePipeSpace = readExample.indexOf(" | ");

							Assert.assertTrue(indexOfSpacePipeSpace > 0);

							String[] labelAndAllFeatures = readExample.split(" \\| ");

							Assert.assertEquals(2, labelAndAllFeatures.length);

							exampleBuilder.setLabel(labelAndAllFeatures[0]);

							String allFeaturesString = labelAndAllFeatures[1];

							String[] individualFeatures = allFeaturesString.split(" ");

							for (String individualFeature : individualFeatures) {
								namespaceBuilder.addFeature(individualFeature);
							}

							exampleBuilder.addNamespace(namespaceBuilder.build());

							return exampleBuilder.build();
						}
					}
					else
						return endOfData();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		};

	}
}
