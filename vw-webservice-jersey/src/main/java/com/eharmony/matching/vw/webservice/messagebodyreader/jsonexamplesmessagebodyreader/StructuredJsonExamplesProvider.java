/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.common.example.Example;
import com.eharmony.matching.vw.webservice.common.example.ExampleFormatException;
import com.eharmony.matching.vw.webservice.common.example.StructuredExample;
import com.eharmony.matching.vw.webservice.common.example.StructuredExample.Namespace;
import com.eharmony.matching.vw.webservice.common.example.StructuredExample.Namespace.NamespaceBuilder;
import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.google.common.collect.AbstractIterator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * @author vrahimtoola
 * 
 *         An example reader writer for the Json format. The Json
 *         format is expected to adhere to the format specified in
 *         vw_example_schema.json, placed under src/test/resources.
 *         The reason it's been placed under src/test/resources as opposed to
 *         src/main/resources is that right now, this schema
 *         file is only being used to document the schema, but not being in a
 *         programmatic way to verify schema adherence (this is
 *         being done via hand-coded logic).
 */
public class StructuredJsonExamplesProvider implements JsonExamplesProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(StructuredJsonExamplesProvider.class);

	/*
	 * The maximum number of features to read into a given namespace.
	 */
	private final int maxNumberOfFeaturesPerNamespace;

	/*
	 * The maximum number of namespaces to read into a given example.
	 */
	private final int maxNumberOfNamespacesPerExample;

	/*
	 * Constructor.
	 * 
	 * @param maxNumberOfFeaturesPerNamespace <= 0 or Integer.MAX_VALUE mean
	 * there's no limit.
	 * 
	 * @param maxNumberOfNamespacesPerExample <= 0 or Integer.MAX_VALUE mean
	 * there's no limit.
	 */
	public StructuredJsonExamplesProvider(int maxNumberOfFeaturesPerNamespace, int maxNumberOfNamespacesPerExample) {
		this.maxNumberOfFeaturesPerNamespace = maxNumberOfFeaturesPerNamespace;
		this.maxNumberOfNamespacesPerExample = maxNumberOfNamespacesPerExample;
	}

	private StructuredExample readExample(long exampleNumber, JsonReader jsonReader) throws IOException {

		jsonReader.beginObject();

		boolean labelRead = false;
		boolean namespacesRead = false;
		boolean tagRead = false;

		StructuredExample.ExampleBuilder exampleBuilder = new StructuredExample.ExampleBuilder();

		boolean atLeastOnePropertyRead = false;

		while (jsonReader.hasNext()) {

			String propertyNameOriginal = jsonReader.nextName();

			String propertyName = propertyNameOriginal.trim().toLowerCase();

			if (propertyName.equals(StructuredJsonPropertyNames.EXAMPLE_LABEL_PROPERTY)) {

				if (labelRead) {

					throw new ExampleFormatException(exampleNumber, "The 'label' property must only appear once in an example!");

				}

				if (jsonReader.peek() != JsonToken.NULL)
					exampleBuilder.setLabel(jsonReader.nextString());
				else {
					jsonReader.nextNull();
				}

				labelRead = true;

				atLeastOnePropertyRead = true;

			}
			else if (propertyName.equals(StructuredJsonPropertyNames.EXAMPLE_TAG_PROPERTY)) {

				if (tagRead) throw new ExampleFormatException(exampleNumber, "The 'tag' property must only appear once in an example!");

				if (jsonReader.peek() != JsonToken.NULL)
					exampleBuilder.setTag(jsonReader.nextString());
				else {
					jsonReader.nextNull();
				}
			}
			else if (propertyName.equals(StructuredJsonPropertyNames.EXAMPLE_NAMESPACES_PROPERTY)) {

				if (namespacesRead) {

					throw new ExampleFormatException(exampleNumber, "The 'namespaces' property must only appear once in an example!");
				}

				if (jsonReader.peek() != JsonToken.NULL) {

					jsonReader.beginArray();

					int numNamespacesRead = 0;

					while (jsonReader.hasNext()) {

						Namespace namespace = readNamespace(exampleNumber, jsonReader);

						numNamespacesRead++;

						if (maxNumberOfNamespacesPerExample > 0 && maxNumberOfNamespacesPerExample < Integer.MAX_VALUE && numNamespacesRead > maxNumberOfNamespacesPerExample) {
							throw new ExampleFormatException(exampleNumber, "The maximum number of namespaces per example, " + maxNumberOfNamespacesPerExample + " was exceeded!");
						}

						exampleBuilder.addNamespace(namespace);
					}

					jsonReader.endArray();

				}
				else {
					jsonReader.nextNull();
				}

				namespacesRead = true;

				atLeastOnePropertyRead = true;

			}
			else {

				throw new ExampleFormatException(exampleNumber, "Unknown property: " + propertyNameOriginal + " found while reading example!");
			}

		}

		jsonReader.endObject();

		if (atLeastOnePropertyRead == false)
			return StructuredExample.EMPTY_EXAMPLE;
		else
			return exampleBuilder.build(); //this might return a normal example or a PIPE example.
	}

	private Namespace readNamespace(long exampleNumber, JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();

		StructuredExample.Namespace.NamespaceBuilder nsBuilder = new StructuredExample.Namespace.NamespaceBuilder();

		boolean nameRead = false, scalingFactorRead = false, featuresRead = false;

		while (jsonReader.hasNext()) {

			String propertyNameOriginal = jsonReader.nextName();
			String propertyName = propertyNameOriginal.trim().toLowerCase();

			if (propertyName.equals(StructuredJsonPropertyNames.NAMESPACE_NAME_PROPERTY)) {

				if (nameRead) {

					throw new ExampleFormatException(exampleNumber, "The 'name' property must only appear once in a namespace!");
				}

				if (jsonReader.peek() == JsonToken.NULL)
					jsonReader.nextNull();
				else {
					String namespace = jsonReader.nextString();
					nsBuilder.setName(namespace);
				}
				nameRead = true;
			}
			else if (propertyName.equals(StructuredJsonPropertyNames.NAMESPACE_SCALING_FACTOR_PROPERTY)) {

				if (scalingFactorRead) {

					throw new ExampleFormatException(exampleNumber, "The 'value' property must only appear once in a namespace!");
				}

				if (jsonReader.peek() == JsonToken.NULL)
					jsonReader.nextNull();
				else {
					double scalingFactor = jsonReader.nextDouble();
					nsBuilder.setScalingFactor(Float.valueOf((float) scalingFactor));
				}
				scalingFactorRead = true;

			}
			else if (propertyName.equals(StructuredJsonPropertyNames.NAMESPACE_FEATURES_PROPERTY)) {

				if (featuresRead) {

					throw new ExampleFormatException(exampleNumber, "The 'features' property must only appear once in a namespace!");
				}

				if (jsonReader.peek() == JsonToken.NULL) {
					jsonReader.nextNull();
				}
				else {

					jsonReader.beginArray();

					int numFeaturesAdded = 0;

					while (jsonReader.hasNext()) {
						readFeatureIntoNamespace(exampleNumber, nsBuilder, jsonReader);

						numFeaturesAdded++;

						if (maxNumberOfFeaturesPerNamespace > 0 && maxNumberOfFeaturesPerNamespace < Integer.MAX_VALUE && numFeaturesAdded > maxNumberOfFeaturesPerNamespace) {
							throw new ExampleFormatException(exampleNumber, "The maximum number of features per namespace, " + maxNumberOfFeaturesPerNamespace + " was exceeded!");
						}
					}

					jsonReader.endArray();

				}
				featuresRead = true;

			}
			else {
				throw new ExampleFormatException(exampleNumber, "Unknown property: " + propertyNameOriginal + " found while reading namespace!");
			}
		}

		jsonReader.endObject();

		return nsBuilder.build();
	}

	private void readFeatureIntoNamespace(long exampleNumber, NamespaceBuilder nsBuilder, JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();

		String name = null;
		Float value = null;

		boolean nameRead = false, valueRead = false;

		while (jsonReader.hasNext()) {

			String propertyNameOriginal = jsonReader.nextName();

			String propertyName = propertyNameOriginal.toLowerCase();

			if (propertyName.equals(StructuredJsonPropertyNames.FEATURE_NAME_PROPERTY)) {

				if (nameRead) {

					throw new ExampleFormatException(exampleNumber, "The 'name' property can only appear once in a feature!");
				}

				name = jsonReader.nextString(); //feature name should never be null, so not doing the null check here. if it's null, let the exception
												//be propagated.

				nameRead = true;

			}
			else if (propertyName.equals(StructuredJsonPropertyNames.FEATURE_VALUE_PROPERTY)) {

				if (valueRead) {

					throw new ExampleFormatException(exampleNumber, "The 'value' property can only appear once in a feature!");
				}

				if (jsonReader.peek() == JsonToken.NULL)
					jsonReader.nextNull();
				else
					value = Float.valueOf((float) jsonReader.nextDouble());

				valueRead = true;

			}
			else {

				throw new ExampleFormatException(exampleNumber, "Unknown property: " + propertyNameOriginal + " found while reading feature!");
			}

		}

		jsonReader.endObject();

		if (StringUtils.isBlank(name) == false) //add feature only if the name exists.
			nsBuilder.addFeature(name, value);
	}

	@Override
	public Iterator<Example> getExamplesFromStream(InputStream inputStream) throws ExampleReadException {

		checkNotNull(inputStream);

		final InputStream theInputStream = inputStream;

		return new AbstractIterator<Example>() {

			private boolean didBeginArray = false;

			private long currentExampleNumber = 1;

			private TracingJsonReader jsonReader;

			private boolean closeReader = false;

			@Override
			protected StructuredExample computeNext() {

				try {
					if (!didBeginArray) {

						jsonReader = new TracingJsonReader(new InputStreamReader(theInputStream), LOGGER.isTraceEnabled());

						jsonReader.beginArray();
						didBeginArray = true;
					}

					if (jsonReader.hasNext()) {

						StructuredExample toReturn = readExample(currentExampleNumber++, jsonReader);

						jsonReader.reset(); //prepare for next example

						return toReturn;
					}
					else {

						jsonReader.endArray();

						closeReader = true;

						return (StructuredExample) endOfData();
					}
				}
				catch (ExampleFormatException ee) {

					if (LOGGER.isTraceEnabled()) {
						try {
							LOGGER.error("Erroneous JSON example: {}", jsonReader.getAllJsonReadSoFar());
						}
						catch (IOException e) {
							LOGGER.error("Failed to spit out erroneous JSON example!", e);
						}
					}

					closeReader = true;

					//make sure that we've set the example number, useful for debugging
					ee.setExampleNumber(currentExampleNumber - 1);

					throw ee;
				}
				catch (Exception e) {

					closeReader = true;

					if (jsonReader != null) try {

						LOGGER.error("Example read exception when attempting to read example number {} - tracing json output: {}", currentExampleNumber - 1, jsonReader.getAllJsonReadSoFar());
					}
					catch (IOException e1) {
						LOGGER.error("Error: {}", e1);
					}

					throw new ExampleReadException(e);
				}
				finally {

					if (closeReader) try {
						if (jsonReader != null) {
							jsonReader.close();
						}
					}
					catch (Exception e2) {
						LOGGER.warn("Error closing JSON reader! Message: {}", e2.getMessage(), e2);
					}
				}

			}
		};

	}

}
