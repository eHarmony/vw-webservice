/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * @author vrahimtoola
 * 
 *         A Json reader that writes everything it reads about an example, into
 *         a json writer. The only time it will not do this is when skipValue()
 *         is called, in which case the string "(SKIPPED VALUE)" is written out
 *         instead.
 * 
 *         The JsonReader uses Gson because I like it's API a lot better than
 *         Jackson's, which I find to be lower level than Gson's. However, with
 *         Gson the writer will not write out just the fieldname, you have to
 *         give it the value of the field as well. Even if you call flush() on
 *         it, it still won't write out just a fieldname because that's not
 *         valid Json (you need the value to go along with it).
 * 
 *         For the purpose of debugging, I want ALL the Json exactly as it's
 *         been read, and the Jackson writer lets you spit out just the
 *         fieldnames if need be, without forcing you to supply a null value.
 * 
 *         It's not ideal since it requires the project to pull in 2 separate
 *         libraries for the same aspect (JSON processing), but I'm not going to
 *         fuss over it for the moment...
 */
public class TracingJsonReader extends JsonReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TracingJsonReader.class);

	private final String SKIPPED_VALUE_STRING = "(SKIPPED VALUE)";

	private StringWriter exampleJsonWriter;
	private JsonGenerator debugWriter;

	public TracingJsonReader(Reader in, boolean isTracingEnabled) throws IOException {
		super(in);

		checkNotNull(in);

		if (isTracingEnabled) {
			this.exampleJsonWriter = new StringWriter();
			this.debugWriter = new JsonFactory().createGenerator(exampleJsonWriter);
		}

	}

	/*
	 * Returns all the Json that has been read in thus far.
	 */
	public String getAllJsonReadSoFar() throws IOException {
		if (debugWriter != null) {
			debugWriter.flush();
			return exampleJsonWriter.toString();
		}

		return "";
	}

	/*
	 * Resets the string writer, making this instance ready for the next
	 * example.
	 */
	public void reset() throws IOException {

		if (debugWriter != null) { //if the debugwriter is null, then tracing is not enabled, so don't do anything.
			exampleJsonWriter.getBuffer().setLength(0);
		}
	}

	@Override
	public void beginArray() throws IOException {
		super.beginArray();

		if (debugWriter != null) debugWriter.writeStartArray();

	}

	@Override
	public void beginObject() throws IOException {
		super.beginObject();

		if (debugWriter != null) debugWriter.writeStartObject();
	}

	@Override
	public void close() throws IOException {
		super.close();

		if (debugWriter != null) debugWriter.close();
	}

	@Override
	public void endArray() throws IOException {
		super.endArray();

		if (debugWriter != null) debugWriter.writeEndArray();
	}

	@Override
	public void endObject() throws IOException {
		super.endObject();

		if (debugWriter != null) debugWriter.writeEndObject();
	}

	@Override
	public boolean hasNext() throws IOException {
		return super.hasNext();
	}

	@Override
	public boolean nextBoolean() throws IOException {
		boolean toReturn = super.nextBoolean();

		if (debugWriter != null) debugWriter.writeBoolean(toReturn);

		return toReturn;
	}

	@Override
	public double nextDouble() throws IOException {
		double toReturn = super.nextDouble();

		if (debugWriter != null) debugWriter.writeNumber(toReturn);

		return toReturn;
	}

	@Override
	public int nextInt() throws IOException {
		int toReturn = super.nextInt();

		if (debugWriter != null) debugWriter.writeNumber(toReturn);

		return toReturn;
	}

	@Override
	public long nextLong() throws IOException {
		long toReturn = super.nextLong();

		if (debugWriter != null) debugWriter.writeNumber(toReturn);

		return toReturn;
	}

	@Override
	public String nextName() throws IOException {
		String toReturn = super.nextName();

		if (debugWriter != null) debugWriter.writeFieldName(toReturn);

		return toReturn;
	}

	@Override
	public void nextNull() throws IOException {
		super.nextNull();

		if (debugWriter != null) debugWriter.writeNull(); //write out a null, so we stay in sync with the reader.
	}

	@Override
	public String nextString() throws IOException {
		String toReturn = super.nextString();

		if (debugWriter != null) debugWriter.writeString(toReturn);

		return toReturn;
	}

	@Override
	public JsonToken peek() throws IOException {
		return super.peek();
	}

	@Override
	public void skipValue() throws IOException {
		super.skipValue();

		//write out a null, so we stay in sync with the reader
		if (debugWriter != null) debugWriter.writeString(SKIPPED_VALUE_STRING);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
