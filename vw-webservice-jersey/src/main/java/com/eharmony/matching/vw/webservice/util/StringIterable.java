/**
 * 
 */
package com.eharmony.matching.vw.webservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author vrahimtoola
 * An implementation of Iterable<String> that returns an iterator to iterate over the lines of a given chunk of text.
 * FYI the iterator returned is not thread safe.
 */
public class StringIterable implements Iterable<String> {

	private String theText;
	
	/*
	 * Constructor.
	 * @param chunkOfText The text to iterate over. Cannot be null (but can be empty).
	 */
	public StringIterable(String chunkOfText)
	{
		if (chunkOfText == null)
			throw new IllegalArgumentException("'chunkOfText' cannot be null!");
		
		theText = chunkOfText;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {

		return new StringBufferedReaderIterator(theText);
	}

	/*
	 * The iterator. Uses a BufferedReader to read lines one at a time from the chunk of text.
	 * Note that this iterator is not thread-safe.
	 */
	private static class StringBufferedReaderIterator implements Iterator<String>
	{
		private BufferedReader bufferedReader = null;
		
		private String nextLineToReturn = null;
		
		private boolean faultedOrClosed = false;
		
		public StringBufferedReaderIterator(String theText)
		{
			bufferedReader = new BufferedReader(new StringReader(theText));
			advance();
		}
		
		@Override
		public boolean hasNext() {
			
			return nextLineToReturn != null;
		}

		@Override
		public String next() {
			
			if (nextLineToReturn == null)
				throw new NoSuchElementException("No element to return! Make sure 'hasNext()' has been called and it returned 'true' before invoking this method.");
			
			String toReturnString = nextLineToReturn; //save reference, since the call to 'advance' below updates 'nextLineToReturn'.
			
			advance();
			
			return toReturnString;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("The 'remove' operation is not supported.");
		}
		
		
		private void advance()
		{
			if (faultedOrClosed)
				return;
			
			try {
				nextLineToReturn = bufferedReader.readLine();
				
				if (nextLineToReturn == null) //close the bufferedReader if no more lines to return
				{
					bufferedReader.close();
					faultedOrClosed = true; //so that we don't try again to read from the bufferedReader
				}
				
			} catch (IOException e) {
				faultedOrClosed = true; //so that we don't try again to read from the bufferedReader
				nextLineToReturn = null; //so that an exception is thrown if someone calls 'next()'.
			}
	
		}
	}
	
}
