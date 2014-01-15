/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.protobufexamplesmessagebodyreader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.ExamplesIterableImpl;
import com.eharmony.matching.vw.webservice.core.example.Example;
import com.eharmony.matching.vw.webservice.core.example.ProtoBufStringWrapperVWExample;
import com.eharmony.matching.vw.webservice.core.example.StringWrappingProtoBufExample;
import com.google.common.collect.AbstractIterator;

/**
 * @author vrahimtoola
 * 
 *         A message body reader that can read a stream of protobuf examples
 *         (ProtoBufStringWrapperVWExample.StringWrapperExample) from an HTTP
 *         request.
 */
@Consumes({ ExampleMediaTypes.SIMPLE_PROTOBUF_1_0 })
@Provider
public class ProtoBufExamplesMessageBodyReader implements MessageBodyReader<ExamplesIterable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtoBufExamplesMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

		LOGGER.debug("Called with media type: {} and type: {}", mediaType.toString(), type);

		boolean willReturn = mediaType.toString().equals(ExampleMediaTypes.SIMPLE_PROTOBUF_1_0) && type == ExamplesIterable.class;

		LOGGER.debug("Returning: {}", willReturn);

		return willReturn;
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
	public ExamplesIterable readFrom(Class<ExamplesIterable> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

		final InputStream theStream = entityStream;

		if (LOGGER.isDebugEnabled()) if (httpHeaders != null && httpHeaders.size() > 0) {
			LOGGER.debug("Rec'd HTTP headers: ");

			for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				LOGGER.debug("{}:{}", entry.getKey(), StringUtils.join(entry.getValue(), ','));
			}
		}

		AbstractIterator<Example> theIterator = new AbstractIterator<Example>() {

			private final ProtoBufStringWrapperVWExample.StringWrapperExample.Builder readingBuilder = ProtoBufStringWrapperVWExample.StringWrapperExample.newBuilder();

			@Override
			protected Example computeNext() {

				readingBuilder.clear();

				try {
					boolean success = readingBuilder.mergeDelimitedFrom(theStream);

					if (success) return new StringWrappingProtoBufExample(readingBuilder.build());
				}
				catch (Exception e) {
					LOGGER.error("Error reading simple protobuf example! Message: {}", e.getMessage(), e);
				}

				return endOfData();
			}
		};

		// TODO: provide the proper number of examples here
		// setting this to Integer.MAX_VALUE for now to force streaming
		return new ExamplesIterableImpl(Integer.MAX_VALUE, null, theIterator);
	}

}
