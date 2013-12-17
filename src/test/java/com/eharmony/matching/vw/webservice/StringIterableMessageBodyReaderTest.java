/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.junit.Test;

/**
 * @author vrahimtoola
 * Tests the StringIterableMessageBodyReader.
 */
public class StringIterableMessageBodyReaderTest {
	
	/*
	 * Tests that spaces at the beginning don't mess things up. Also tries different newline characters.
	 */
	@Test
	public void spacesAtBeginningAndDifferentNewlinesTest() throws WebApplicationException, IOException {

		for (String newLineToUse : new String[] {"\n", "\r", "\r\n"})
		{
			StringIterableMessageBodyReader toTest = new StringIterableMessageBodyReader();
			
			MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;
			
			StringBuilder theExamples = new StringBuilder();
			theExamples.append("  vwExamples=Example 1");
			theExamples.append(newLineToUse);
			theExamples.append("Example 2");
			theExamples.append(newLineToUse);
			theExamples.append("Example3 and 4 and 5");
			theExamples.append(newLineToUse);
			theExamples.append(newLineToUse);
			
			//note: data needs to be encoded using the correct char set, which must match the mediatype.
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(theExamples.toString().getBytes(ReaderWriter.getCharset(mediaType)));
		
			//the readFrom method only looks at the mediatype and the input stream, so other params can be null.
			Iterable<String> theIterable = toTest.readFrom(null, null, null, mediaType, null, byteArrayInputStream);
			
			int x = 0;
			for (String example: theIterable)
			{
				switch(x++)
				{
					case 0:
						Assert.assertEquals("Example 1", example);
						break;
						
					case 1:
						Assert.assertEquals("Example 2", example);
						break;
						
					case 2:
						Assert.assertEquals("Example3 and 4 and 5", example);
						break;
						
					case 3:
						Assert.assertEquals("", example);
						break;
						
					default:
						Assert.fail();
				}
			}
			
			Assert.assertEquals(4, x);	
		
		}
	
	}
	
	/*
	 * Tests a massive number of examples using the ner.train.gz training set 
	 * from the vowpal wabbit github repository.
	 * This training set was taken from the '/test/train-sets/' subfolder of the
	 * vowpal wabbit github repo (git@github.com:JohnLangford/vowpal_wabbit.git).
	 * I made a slight modification to the very first example in that file.
	 * I added the "vwExamples=" marker to the first example, because the StringIterableMessageBodyReader
	 * expects that the stream of examples will begin with this marker.
	 * The entire file was then saved to ner.train.withVwExamplesMarkerPrePended.gz.
	 */
	@Test
	public void hugeFileTest() throws IOException
	{
		//pass the gzip inputstream to the string message body reader, and at the same time, read from the file. 
		//then compare the examples read in to verify that they match.
		
		//the input stream to read directly from the file
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.withVwExamplesMarkerPrePended.gz"));
		
		BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream));
		
		//the input stream that the StringIterableMessageBodyReader will use.
		GZIPInputStream gzipInputStreamForTestSubject = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.withVwExamplesMarkerPrePended.gz"));

		StringIterableMessageBodyReader toTest = new StringIterableMessageBodyReader();
		
		MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;	
		
		Iterable<String> theIterableOfExamples = toTest.readFrom(null, null, null, mediaType, null, gzipInputStreamForTestSubject);
		
		//the first line in our test file contains 'vwExamples=', read it and remove that marker.
		//we have to to this because the iterable will return examples without the 'vwExamples=' marker.
		String expectedExample = testReader.readLine();
		expectedExample = expectedExample.substring("vwexamples=".length());
		
		int numExamples = 0;
		
		boolean dumpExamples = false; //turn on to see some examples
		
		for (String example:theIterableOfExamples)
		{
			Assert.assertEquals(expectedExample, example);
			
			numExamples++;
			
			if (dumpExamples && numExamples % 21 == 0) //print every 21st example
			{
				//TODO: get rid of system.out.println !!
				
				//TODO: get a jenkin's build going, turn on code coverage + findbugs etc etc
				
				System.out.println("expected example: " + expectedExample);
				System.out.println("read example    : " + example);
				System.out.println();
			}
			
			expectedExample = testReader.readLine(); //move to next expected example
		}
		
		Assert.assertTrue(testReader.readLine() == null); //ensure all examples read and verified.
	}
	
	

}
