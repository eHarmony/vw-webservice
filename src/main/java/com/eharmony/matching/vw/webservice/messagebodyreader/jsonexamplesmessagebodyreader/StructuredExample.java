/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.ExampleFormatException;
import com.google.common.collect.ImmutableList;

/**
 * @author vrahimtoola
 * 
 *         An example to be submitted to VW. A structured example can be queried
 *         to get the various components that make up the example that will be
 *         submitted to VW.
 */
public class StructuredExample implements Example {

	/*
	 * The various kinds of examples.
	 */
	private enum ExampleType {

		/*
		 * The empty example. This will be sent to VW as a single newline
		 * character.
		 */
		EMPTY,

		/*
		 * The pipe (|) example. This will be sent to VW as a single pipe
		 * character, ie, '|'.
		 */
		PIPE,

		/*
		 * A normal VW example.
		 */
		NORMAL
	}

	/*
	 * Some pre-defined examples.
	 */

	/*
	 * The empty example. This will be sent to VW as a single newline character.
	 */
	public static final StructuredExample EMPTY_EXAMPLE = new StructuredExample(ExampleType.EMPTY, "", "", new ArrayList<StructuredExample.Namespace>());

	/*
	 * The pipe (|) example. This will be sent to VW as a single character, '|'.
	 */
	public static final StructuredExample PIPE_EXAMPLE = new StructuredExample(ExampleType.PIPE, "", "", new ArrayList<StructuredExample.Namespace>());

	private final ExampleType exampleType;
	private final String label;
	private final Iterable<Namespace> namespaces;
	private final String tag;

	private StructuredExample(ExampleType exampleType, String label, String tag, Iterable<Namespace> namespaces) {
		this.exampleType = exampleType;
		this.label = label;
		this.namespaces = namespaces;
		this.tag = tag;
	}

	/*
	 * Returns the label of this example.
	 * 
	 * @returns The label of this example.
	 */
	public String getLabel() {
		return label;
	}

	/*
	 * Returns the tag of this example.
	 * 
	 * @returns The tag of this example.
	 */
	public String getTag() {
		return tag;
	}

	/*
	 * Returns the namespaces in this example.
	 * 
	 * @returns The namespaces in this example. The returned iterable is
	 * unmodifiable.
	 */
	public Iterable<Namespace> getNamespaces() {
		return namespaces;
	}

	@Override
	public String getVWStringRepresentation() {

		if (exampleType == ExampleType.EMPTY)
			return "";
		else if (exampleType == ExampleType.PIPE)
			return " |"; //note the space before the pipe
		else {

			final String SPACE = " ";
			final String PIPE = "|";
			final String COLON = ":";

			StringBuilder builder = new StringBuilder();

			if (label != null) {
				builder.append(label);
				builder.append(SPACE);
			}

			if (tag != null) {
				builder.append(tag);
			}

			boolean namespacesAdded = false;

			for (Namespace namespace : namespaces) {

				if (namespacesAdded) builder.append(SPACE);

				builder.append(PIPE);

				String namespaceName = namespace.getName();

				if (StringUtils.isBlank(namespaceName) == false) builder.append(namespaceName);

				if (namespace.getScalingFactor() != null) {
					builder.append(COLON);

					Float scalingFactor = namespace.getScalingFactor();

					//this will take care of getting rid of extraneous 0s, eg, 12.3400000
					if (scalingFactor.floatValue() == (int) scalingFactor.floatValue())
						builder.append(String.format("%d", (int) scalingFactor.floatValue()));
					else {
						builder.append(String.format("%s", scalingFactor.floatValue()));
					}
				}

				for (StructuredExample.Namespace.Feature feature : namespace.getFeatures()) {

					builder.append(SPACE);

					String featureName = feature.getName();
					Float featureValue = feature.getValue();

					builder.append(featureName);

					if (featureValue != null) {
						builder.append(COLON);

						//this will take care of getting rid of extraneous 0s, eg, 12.3400000
						if (featureValue.floatValue() == (int) featureValue.floatValue())
							builder.append(String.format("%d", (int) featureValue.floatValue()));
						else {
							builder.append(String.format("%s", featureValue.floatValue()));
						}

					}

				}

				namespacesAdded = true;
			}

			//if there's a label but no namespaces, add a SPACE and a PIPE after the label
			if (!namespacesAdded) {
				builder.append(PIPE);
			}

			return builder.toString();
		}
	}

	/*
	 * Represents a namespace containing 0 or more features. Instances of this
	 * class are immutable.
	 */
	public static class Namespace {

		private final List<Feature> features;
		private final String namespaceName;
		private final Float scalingFactor;

		private Namespace(String namespaceName, Float scalingFactor, List<Feature> features) {
			this.namespaceName = namespaceName == null ? null : namespaceName.trim();
			this.scalingFactor = scalingFactor;
			this.features = features;
		}

		/*
		 * Returns the features of the map.
		 * 
		 * @returns The list of features. The list is unmodifiable.
		 */
		public Iterable<Feature> getFeatures() {
			return features;
		}

		/*
		 * Returns the number of features in this namespace.
		 * 
		 * @returns The number of features in this namespace. Always >= 0.
		 */
		private int getNumberOfFeatures() {
			return (features == null ? 0 : features.size());
		}

		/*
		 * Returns the name of this namespace.
		 * 
		 * @returns The name of this namespace. Can be null/empty.
		 */
		public String getName() {
			return namespaceName;
		}

		/*
		 * Returns the scaling factor of this namespace. Can be null, which is
		 * the same as 1.0 (as per VW documentation).
		 * 
		 * @returns The scaling factor for this namespace. Can be null.
		 */
		public Float getScalingFactor() {
			return scalingFactor;
		}

		/*
		 * Represents a single feature inside a namespace.
		 */
		public static class Feature {
			private final String name;
			private final Float value;

			private Feature(String name, Float value) {
				this.name = name.trim();
				this.value = value;
			}

			/*
			 * Returns the name of this feature.
			 * 
			 * @returns The name of this feature.
			 */
			public String getName() {
				return name;
			}

			/*
			 * Returns the value of this feature. The value can be null.
			 * 
			 * @returns The value of this feature. Can be null.
			 */
			public Float getValue() {
				return value;
			}
		}

		/*
		 * Builds a single namespace of an example. Instances of this class are
		 * not thread safe. A NamespaceBuilder can be used repeatedly to build
		 * namespace instances. Just make sure to call 'clear()' before starting
		 * to build up the second (or subsequent) namespace. Note that invoking
		 * 'build' does not implicitly invoke 'clear()' after a Namespace has
		 * been built; 'clear()' must be invoked explicitly.
		 */
		public static class NamespaceBuilder {

			private List<Feature> features = null;
			private String namespaceName;
			private Float scalingFactor;

			/*
			 * Sets the name for the namespace being built.
			 * 
			 * @param namespaceName The name of the namespace being built. Can
			 * be null/empty. From the VW documentation: Currently, the only
			 * characters that can't be used in feature or namespace names are
			 * vertical bar, colon, space, and newline.
			 * 
			 * @returns This builder.
			 */
			public NamespaceBuilder setName(String namespaceName) {

				if (namespaceName != null) {
					if (namespaceName.contains("|") || namespaceName.contains(":") || StringUtils.containsWhitespace(namespaceName)) {
						throw new ExampleFormatException("The namespace name cannot contain whitespace, '|' or ':'! Namespace passed in was: " + namespaceName);
					}
				}

				this.namespaceName = namespaceName;
				return this;
			}

			/*
			 * Sets the scaling factor for this namespace.
			 * 
			 * @param scalingFactor The scaling factor. Can be null (which VW
			 * will interpret as 1.0).
			 * 
			 * @returns This builder.
			 */
			public NamespaceBuilder setScalingFactor(Float scalingFactor) {
				this.scalingFactor = scalingFactor;
				return this;
			}

			/*
			 * Adds a feature to this namespace. The value of the feature will
			 * default to 1.0, as per the VW documentation.
			 * 
			 * @param feature The feature name to be added.
			 */
			public NamespaceBuilder addFeature(String feature) {
				return addFeature(feature, null);
			}

			/*
			 * Adds a feature with the specified value to the namespace.
			 * 
			 * @param feature The feature to add. Cannot be null/empty. From the
			 * VW documentation: Currently, the only characters that can't be
			 * used in feature or namespace names are vertical bar, colon,
			 * space, and newline.
			 * 
			 * @param value The float value of the feature.
			 * 
			 * @returns This builder.
			 */
			public NamespaceBuilder addFeature(String feature, Float value) {
				if (StringUtils.isBlank(feature)) throw new ExampleFormatException("The feature name must be provided!");

				if (feature.contains("|") || feature.contains(":") || StringUtils.containsWhitespace(feature))
					throw new ExampleFormatException("The feature name cannot contain whitespace, '|' or ':'! Feature name passed in was: " + feature);

				if (features == null) features = new ArrayList<StructuredExample.Namespace.Feature>();
				features.add(new Feature(feature, value));
				return this;
			}

			/*
			 * Removes all features from the namespace.
			 * 
			 * @returns This builder.
			 */
			public NamespaceBuilder clear() {
				features = null;
				scalingFactor = null;
				namespaceName = null;
				return this;
			}

			/*
			 * Builds the namespace with a scaling factor of 1.0.
			 * 
			 * @returns The newly built namespace.
			 */
			public Namespace build() {

				if (StringUtils.isBlank(namespaceName) && scalingFactor != null) throw new ExampleFormatException("A namespace with a scaling factor must be given a name!");

				return new Namespace(namespaceName, scalingFactor, features == null ? new ArrayList<Feature>() : ImmutableList.<Feature> builder().addAll(features).build());
			}
		}

	}

	public static class ExampleBuilder {

		private boolean atLeastOneNamespaceIsNonBlank = false;
		private String label = null;
		private List<Namespace> namespaces = null;
		private String tag = null;

		/*
		 * Sets the label for the example.
		 * 
		 * @param label The label for the example. Can be null/empty. Will be
		 * trimmed (ie, trim() will be called on it) when the example is
		 * created.
		 * 
		 * @returns This ExampleBuilder.
		 */
		public ExampleBuilder setLabel(String label) {
			this.label = label;
			return this;
		}

		/*
		 * Sets the tag for this example.
		 * 
		 * @param tag The tag for the example. Can be null/empty. Will be
		 * trimmed when the example is created.
		 * 
		 * @returns This ExampleBuilder.
		 */
		public ExampleBuilder setTag(String tag) {
			this.tag = tag;
			return this;
		}

		/*
		 * Clears the builder, thus making it ready for use to create the next
		 * example.
		 * 
		 * @returns This ExampleBuilder.
		 */
		public ExampleBuilder clear() {
			label = null;
			namespaces = null;
			tag = null;
			return this;
		}

		/*
		 * Adds a namespace to the example.
		 * 
		 * @param namespace The namespace to add to the builder. Cannot be null.
		 * 
		 * @returns This builder.
		 */
		public ExampleBuilder addNamespace(Namespace namespace) {
			checkNotNull(namespace);
			if (namespaces == null) namespaces = new ArrayList<StructuredExample.Namespace>();
			namespaces.add(namespace);
			atLeastOneNamespaceIsNonBlank = namespace.getNumberOfFeatures() > 0 || (StringUtils.isBlank(namespace.getName()) == false);
			return this;
		}

		/*
		 * Builds and returns the example.
		 */
		public StructuredExample build() {
			if (label != null) label = label.trim();
			if (tag != null) tag = tag.trim();

			//If no label and no namespaces (or all namespaces are empty), treat it as the pipe example.
			if (StringUtils.isBlank(label) && StringUtils.isBlank(tag) && (namespaces == null || namespaces.size() == 0 || atLeastOneNamespaceIsNonBlank == false))
				return StructuredExample.PIPE_EXAMPLE;

			return new StructuredExample(ExampleType.NORMAL, label, tag, namespaces == null ? new ArrayList<Namespace>() : ImmutableList.<Namespace> builder().addAll(namespaces).build());

		}
	}

}
