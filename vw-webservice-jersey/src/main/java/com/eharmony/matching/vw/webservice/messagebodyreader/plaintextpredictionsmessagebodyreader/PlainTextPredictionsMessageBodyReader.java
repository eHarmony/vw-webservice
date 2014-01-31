/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.plaintextpredictionsmessagebodyreader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Iterator;
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

import com.eharmony.matching.vw.webservice.core.prediction.Prediction;

/**
 * @author vrahimtoola
 * 
 *         A message body reader that can read an Iterable<Prediction> from the
 *         message body of an HTTP response.
 */
@Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
@Provider
public class PlainTextPredictionsMessageBodyReader implements
		MessageBodyReader<Iterable<Prediction>> {

	public PlainTextPredictionsMessageBodyReader() {

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextPredictionsMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

		LOGGER.info("Called with media type: {}", mediaType.toString());

		return true;
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
	public Iterable<Prediction> readFrom(Class<Iterable<Prediction>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

		if (httpHeaders != null && httpHeaders.size() > 0) {
			LOGGER.debug("Rec'd HTTP headers: ");

			for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				LOGGER.debug("{}:{}", entry.getKey(), StringUtils.join(entry.getValue(), ','));
			}
		}

		Charset charset = ReaderWriter.getCharset(mediaType);

		final StringPredictionIterator theIterator = new StringPredictionIterator(entityStream, charset);

		return new Iterable<Prediction>() {

			@Override
			public Iterator<Prediction> iterator() {
				return theIterator;
			}

		};
	}

}
