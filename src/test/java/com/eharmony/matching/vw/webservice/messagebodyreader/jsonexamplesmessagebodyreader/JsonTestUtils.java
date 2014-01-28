/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample.Namespace;
import com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader.StructuredExample.Namespace.Feature;
import com.google.gson.stream.JsonWriter;

/**
 * @author vrahimtoola
 *         Writes out a StructuredExample to JSON format.
 */
public class JsonTestUtils {

	public static void writeExample(JsonWriter jsonWriter, StructuredExample structuredExample) throws IOException {

		jsonWriter.beginObject();

		String label = structuredExample.getLabel();

		//always write the label out, this is how a pipe example is distinguished from an empty example.
		jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_LABEL_PROPERTY);

		if (StringUtils.isBlank(label)) {
			jsonWriter.nullValue();
		}
		else {
			jsonWriter.value(label);
		}

		//for the tag and namespaces properties, only write them if they're non-null
		String tag = structuredExample.getTag();

		if (StringUtils.isBlank(tag) == false) jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_TAG_PROPERTY).value(tag);

		Iterable<Namespace> namespaces = structuredExample.getNamespaces();

		if (namespaces != null) {

			jsonWriter.name(StructuredJsonPropertyNames.EXAMPLE_NAMESPACES_PROPERTY);

			jsonWriter.beginArray();

			for (Namespace ns : namespaces) {
				writeNamespace(ns, jsonWriter);
			}

			jsonWriter.endArray();

		}

		jsonWriter.endObject(); //for the empty example, just write the "{}". 

	}

	private static void writeNamespace(Namespace namespace, JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();

		String name = namespace.getName();
		Float scale = namespace.getScalingFactor();

		if (StringUtils.isBlank(name) == false) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_NAME_PROPERTY).value(name);
		}

		if (scale != null) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_SCALING_FACTOR_PROPERTY).value(scale);
		}

		Iterable<Feature> features = namespace.getFeatures();

		if (features != null) {
			jsonWriter.name(StructuredJsonPropertyNames.NAMESPACE_FEATURES_PROPERTY);

			jsonWriter.beginArray();

			for (Feature feature : features) {
				writeFeature(feature, jsonWriter);
			}

			jsonWriter.endArray();
		}

		jsonWriter.endObject();
	}

	private static void writeFeature(Feature feature, JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();

		String name = feature.getName();
		Float value = feature.getValue();

		if (StringUtils.isBlank(name) == false) {
			jsonWriter.name(StructuredJsonPropertyNames.FEATURE_NAME_PROPERTY).value(name);
		}

		if (value != null) {
			jsonWriter.name(StructuredJsonPropertyNames.FEATURE_VALUE_PROPERTY).value(value);
		}

		jsonWriter.endObject();
	}
}
