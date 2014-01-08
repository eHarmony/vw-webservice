/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextexamplesmessagebodyreader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.ExamplesIterableImpl;

/**
 * @author vrahimtoola
 * 
 *         A message body reader that can read an Iterable<String> from the
 *         message body of an HTTP request.
 */
@Consumes({ MediaType.TEXT_PLAIN, ExampleMediaTypes.PLAINTEXT_1_0 })
@Provider
public class PlainTextExamplesMessageBodyReader implements MessageBodyReader<ExamplesIterable> {

	public PlainTextExamplesMessageBodyReader() {

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextExamplesMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

		LOGGER.debug("Called with media type: {} and type: {}", mediaType.toString(), type);

		boolean willReturn = (mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) || mediaType.toString().equals(ExampleMediaTypes.PLAINTEXT_1_0)) && type == ExamplesIterable.class;

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

		if (LOGGER.isDebugEnabled()) if (httpHeaders != null && httpHeaders.size() > 0) {
			LOGGER.debug("Rec'd HTTP headers: ");

			for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				LOGGER.debug("{}:{}", entry.getKey(), StringUtils.join(entry.getValue(), ','));
			}
		}

		// TODO:
		// if a content-length has been provided, then use that to read entire
		// string in one go.

		Charset charset = ReaderWriter.getCharset(mediaType);

		StringExampleIterator theIterator = new StringExampleIterator(entityStream, charset);

		// TODO: provide the proper number of examples here
		// setting this to Integer.MAX_VALUE for now to force streaming
		return new ExamplesIterableImpl(Integer.MAX_VALUE, null, theIterator);
	}

}
