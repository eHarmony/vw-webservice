/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vrahimtoola
 * A message body reader that can read an Iterable<String> from the message body of an HTTP request.
 */
@Provider
public class StringIterableMessageBodyReader implements MessageBodyReader<Iterable<String>> {

	/*
	 * The max length of a VW example, in plain-text form. 
	 */
	private static final int MAX_URL_ENCODED_EXAMPLE_LENGTH = 3000;
	
	private int maxExampleLength = MAX_URL_ENCODED_EXAMPLE_LENGTH;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StringIterableMessageBodyReader.class);
	
	public StringIterableMessageBodyReader()
	{
		
	}
	
	public StringIterableMessageBodyReader(int maxExampleLength)
	{
		//TODO: look at guava pre conditions
		if (maxExampleLength < 0)
			throw new IllegalArgumentException("The max example length must be non-negative!");
		
		this.maxExampleLength = maxExampleLength;
	}
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {

		LOGGER.info("Called with media type: {}", mediaType);
		
		return mediaType.equals(MediaType.TEXT_PLAIN_TYPE) &&
			 type == Iterable.class; //TODO: add check for generic type
	}

	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 *
	 * Expects data to arrive as url-encoded strings.
	 * 
	 * TODO: look at specific mediatypes eg text/vw
	 */
	@Override
	public Iterable<String> readFrom(Class<Iterable<String>> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		//TODO:
		//if a content-length has been provided, then use that to read entire string in one go.
		
		Charset charset = ReaderWriter.getCharset(mediaType);
		
		final InputStreamReader inputStreamReader = new InputStreamReader(entityStream, charset);
		
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator()
			{
				try {
					return new StringIterator(inputStreamReader, maxExampleLength);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
	}
	
	//TODO look at guava's abstract iterator
	//and the test that comes with guava
	//TODO just read lines (ie, called BufferedReader's readLine)
	private static class StringIterator implements Iterator<String>
	{
		/*
		 * Indicates what state the iterator is in, when reading from the input stream.
		 */
		private enum ReadStates
		{
			/*
			 * Reading the initial spaces, if any, before the 'vwExamples=' marker.
			 */
			InitialSpaces,
			
			/*
			 * Reading the 'vwExamples=' form parameter.
			 */
			VwExamplesMarker,
			
			/*
			 * Reading the VW examples one by one (ie, one line at a time).
			 */
			Examples
		};
		
		/*
		 * The input stream reader.
		 */
		private InputStreamReader reader;
		
		/*
		 * The example to be returned, when 'next()' is called.
		 */
		private String nextExampleToReturn = null;
		
		/*
		 * The maximum length of a VW example.
		 */
		private int maxExampleLength;
		
		/*
		 * The buffer to store read characters into. 
		 */
		private char[] readBuffer = new char[1];

		private StringBuilder exampleBuilder;
		
		private ReadStates currentState = ReadStates.InitialSpaces;
		
		private char extraCharFromPreviousExample;
		private boolean readExtraCharFromPreviousExample = false;
		
		private int MAX_INITIAL_WHITESPACES = 10;
		
		public StringIterator(InputStreamReader reader, int maxExampleLength) throws IOException
		{
			this.reader = reader;
			this.maxExampleLength = maxExampleLength;
			exampleBuilder = new StringBuilder(maxExampleLength);
			advance();
		}
		
		
		@Override
		public boolean hasNext() {
			return nextExampleToReturn != null;
		}

		@Override
		public String next() {

			String toReturn = nextExampleToReturn;
			
			if (toReturn == null)
				throw new NoSuchElementException("No element to return! Make sure to call 'hasNext()' and that it returns true before invoking this method!");
			
			try {
				advance();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return toReturn.trim();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("The 'remove' operation is not supported!");
		}

		private void advance() throws IOException
		{
			boolean keepReading = true;
			
			int numInitialWhitespacesRead = 0;
			
			exampleBuilder.delete(0, exampleBuilder.length()); //clear it out to make room for the next example.
			
			while (keepReading)
				switch(currentState)
				{
					case InitialSpaces:
						int numCharsRead = reader.read(readBuffer);
						
						if (numCharsRead <= 0)
							keepReading = false;
						else {
							if (Character.isWhitespace(readBuffer[0]))
							{
								if (numInitialWhitespacesRead++ > MAX_INITIAL_WHITESPACES)
									throw new IOException("Too many white spaces before start of vw examples!");
							}	
							else if (Character.toLowerCase(readBuffer[0]) == 'v')
							{
								currentState = ReadStates.VwExamplesMarker; //saw the initial 'v', start reading 'vwExamples='
							}
							else {
								throw new IOException("Found another character ('" + readBuffer[0] +"') but was was expecting the 'v' of 'vwExamples'!");
							}	
						}
						
						break;

					case VwExamplesMarker:
						StringBuilder sbr = new StringBuilder();
						
						sbr.append('v'); //we already read the 'v', that's how we got in this state.
						
						//try and read the 'wexamples='.
						char[] tempBuf = new char[10];
						
						numCharsRead = reader.read(tempBuf);
						
						if (numCharsRead < 10)
							throw new IOException("Expected to find 'vwExamples=' for examples, but didn't find enough characters!");
						
						sbr.append(tempBuf);
						
						if (sbr.toString().toLowerCase().equals("vwexamples="))
							currentState = ReadStates.Examples;
						else {
							throw new IOException("Expected to start examples with 'vwexamples=', but instead it starts with: '" + sbr.toString() + "'!");
						}
						
						break;
						
					case Examples:

						//TODO:
						
						//if we read an extra char from the previous example, place it
						//into the stringbuilder for this example that we are reading now.
						if (readExtraCharFromPreviousExample)
						{
							exampleBuilder.append(extraCharFromPreviousExample);
							readExtraCharFromPreviousExample = false;
						}
						
						nextExampleToReturn = null;
						
						numCharsRead = reader.read(readBuffer); //read in 1 character at a time
						
						if (numCharsRead <= 0) //reached end of stream, can't read any more.
						{
							keepReading = false; 
							
							if (exampleBuilder.length() > 0)
								nextExampleToReturn = exampleBuilder.toString();
						}
						else {
							
							//if the char is either a '\n' or '\r', we've read an example.
							if (readBuffer[0] == '\n' || readBuffer[0] == '\r')
							{
								nextExampleToReturn = exampleBuilder.toString();
								keepReading = false;
								
								if (readBuffer[0] == '\r') //an example could end in either '\r\n' or '\n' or '\r'.
														   //so if we see an '\r', try to read the extra '\n', if present.
								{
									numCharsRead = reader.read(readBuffer);
									
									if (numCharsRead > 0)
									{
										if (readBuffer[0] != '\n')
										{
											readExtraCharFromPreviousExample = true;
											extraCharFromPreviousExample = readBuffer[0];
										}
									}
	
								}

							}
							else {

								if (exampleBuilder.length() + readBuffer.length > maxExampleLength)
									throw new IOException("An example exceeding the max example length of " + maxExampleLength + " characters was encountered!");
								
								exampleBuilder.append(readBuffer);
							}
								
						}
						
						break;
				}
		}
		
	}

	
	
}
