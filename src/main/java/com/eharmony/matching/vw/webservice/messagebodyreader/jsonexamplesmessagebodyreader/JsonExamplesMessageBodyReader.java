/**
 * 
 */
package com.eharmony.matching.vw.webservice.messagebodyreader.jsonexamplesmessagebodyreader;

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

/**
 * @author vrahimtoola
 * 
 */
@Consumes({ ExampleMediaTypes.SIMPLE_JSON_1_0 })
@Provider
public class JsonExamplesMessageBodyReader implements MessageBodyReader<ExamplesIterable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonExamplesMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {

		LOGGER.debug("Called with media type: {} and type: {}", mediaType.toString(), type);

		boolean willReturn = mediaType.toString().equals(ExampleMediaTypes.SIMPLE_JSON_1_0) && type == ExamplesIterable.class;

		LOGGER.debug("Returning: {}", willReturn);

		return willReturn;
	}

	@Override
	public ExamplesIterable readFrom(Class<ExamplesIterable> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

		if (LOGGER.isDebugEnabled()) if (httpHeaders != null && httpHeaders.size() > 0) {
			LOGGER.debug("Rec'd HTTP headers: ");

			for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				LOGGER.debug("{}:{}", entry.getKey(), StringUtils.join(entry.getValue(), ','));
			}
		}

		//TODO: hard-coding to GsonJsonExamplesProvider for now
		return new ExamplesIterableImpl(Integer.MAX_VALUE, null, new GsonJsonExamplesProvider().getExamplesFromStream(entityStream));

	}

}
