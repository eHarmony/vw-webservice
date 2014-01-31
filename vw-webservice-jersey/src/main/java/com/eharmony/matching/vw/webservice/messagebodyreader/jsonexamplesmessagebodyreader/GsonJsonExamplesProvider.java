/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.eharmony.matching.vw.webservice.core.ExampleReadException;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.StringExample;
import com.google.common.collect.AbstractIterator;
import com.google.gson.stream.JsonReader;

/**
 * @author vrahimtoola
 * 
 *         Uses Google's GSON to provide json examples.
 */
public class GsonJsonExamplesProvider implements JsonExamplesProvider {

	@Override
	public Iterator<Example> getExamplesFromStream(InputStream inputStream) throws ExampleReadException {

		final JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));

		AbstractIterator<Example> theIterator = new AbstractIterator<Example>() {

			private boolean readStartOfArray = false;

			@Override
			public Example computeNext() {

				try {

					if (!readStartOfArray) {
						jsonReader.beginArray();
						readStartOfArray = true;
					}

					if (jsonReader.hasNext()) {

						return readIndividualJsonExample(jsonReader);

					}
					else {
						jsonReader.endArray();
						return endOfData();
					}
				}
				catch (Exception e) {
					throw new ExampleReadException(e);
				}

			}
		};

		return theIterator;
	}

	private Example readIndividualJsonExample(JsonReader reader) throws IOException {
		reader.beginObject();

		String exampleString = null;

		while (reader.hasNext()) {
			String propertyName = reader.nextName();

			if (propertyName.equalsIgnoreCase("example")) {

				if (exampleString != null) throw new ExampleReadException("The property 'example' was found more than once in a single JSON example!");

				exampleString = reader.nextString();

			}
			else {
				throw new ExampleReadException("Unexpected property name found in JSON example: " + propertyName);
			}
		}

		reader.endObject();

		if (exampleString == null) throw new ExampleReadException("Empty JSON example found!");

		return new StringExample(exampleString);
	}
}
