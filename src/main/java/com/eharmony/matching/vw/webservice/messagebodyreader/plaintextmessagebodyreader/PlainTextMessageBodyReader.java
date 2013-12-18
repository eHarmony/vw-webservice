/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextmessagebodyreader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.ExamplesIterableImpl;

/**
 * @author vrahimtoola A message body reader that can read an Iterable<String>
 *         from the message body of an HTTP request.
 */
@Provider
public class PlainTextMessageBodyReader implements
		MessageBodyReader<ExamplesIterable> {

	public PlainTextMessageBodyReader() {

	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PlainTextMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {

		LOGGER.info("Called with media type: {}", mediaType.toString());

		return (mediaType.toString().equals(MediaType.TEXT_PLAIN) || mediaType
				.toString().equals(ExampleMediaTypes.PLAINTEXT_1_0))
				&& type == ExamplesIterable.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
	 * java.lang.reflect.Type, java.lang.annotation.Annotation[],
	 * javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
	 * java.io.InputStream)
	 * 
	 * Expects data to arrive as url-encoded strings.
	 * 
	 * TODO: look at specific mediatypes eg text/vw
	 */
	@Override
	public ExamplesIterable readFrom(Class<ExamplesIterable> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		// TODO:
		// if a content-length has been provided, then use that to read entire
		// string in one go.

		// for now, always returning async example submitter.

		Charset charset = ReaderWriter.getCharset(mediaType);

		StringExampleIterator theIterator = new StringExampleIterator(
				entityStream, charset);

		// TODO: provide appropriate hints in the passed in map.
		return new ExamplesIterableImpl(null, theIterator);
	}

}
