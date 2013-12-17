/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author vrahimtoola
 * An implementation of VWPredictor that submits examples to, and retrieves predictions from, VW over a TCP-IP socket.
 */
@Component
public class TCPIPVWPredictor implements VWPredictor {

	private String hostNameString;
	private int port;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TCPIPVWPredictor.class);
	
	//TODO: consider using an application-wide thread pool.
	//This threadpool gets created per instance of TCPIPVWPredictor.
	private ExecutorService threadPoolExecutorService = Executors.newCachedThreadPool();
	
	@Autowired
	public TCPIPVWPredictor(@Value("${vw.hostName}") String vwHostName, 
							@Value("${vw.port}") int vwPort)
	{
		if (StringUtils.isBlank(vwHostName))
			throw new IllegalArgumentException("A hostname must be specified!");
		
		if (vwPort < 0)
			throw new IllegalArgumentException("Invalid post specified!");
		
		hostNameString = vwHostName;
		port = vwPort;
	}
	
	/* (non-Javadoc)
	 * @see com.eharmony.matching.vw.webservice.core.VWPredictor#predict(java.lang.Iterable)
	 * 
	 * Each invocation of this method creates a separate TCP-IP socket connection to VW.
	 */
	@Override
	public Iterable<String> predict(Iterable<String> vwExamples) {
		
		if (vwExamples == null)
			throw new IllegalArgumentException("Cannot provide a null iterator of examples!");
		
		try {
			
			LOGGER.info("Connecting to VW...");
			
			final Socket socket = new Socket(hostNameString, port);
			
			//submit examples in a background thread.
			threadPoolExecutorService.submit(new VWExampleSubmitter(socket, vwExamples));
			
			return new VWPredictionIterable(socket);	

		} catch (Exception e) {
			
			LOGGER.error("Exception during prediction! Exception message: " + e.getMessage(), e);
		}

		return null; //empty iterable
	}
	
	/*
	 * Submits examples to VW.
	 */
	private static class VWExampleSubmitter implements Runnable
	{
		private Socket socket;
		private Iterable<String> vwExamples;
		
		private static final Logger LOGGER = LoggerFactory.getLogger(VWExampleSubmitter.class);
		
		public VWExampleSubmitter(Socket socket, Iterable<String> vwExamples)
		{
			this.socket = socket;
			this.vwExamples = vwExamples;
		}
		
		@Override
		public void run() {
			
			PrintWriter vwWriter;
			try {
				//TODO: consider BufferedWriter
				vwWriter = new PrintWriter(socket.getOutputStream(), true);
				
				for (String example : vwExamples)
				{
					if (StringUtils.isBlank(example))
						continue; //skip null/blank examples.
					
					//TODO: BufferedWriter should have a writeLine method...
					//this line is inefficient
					String toSubmit = example.trim() + System.getProperty("line.separator"); 
					
					LOGGER.info("Submitting example to VW: " + toSubmit);
					
					vwWriter.write(toSubmit); 	//need to explicitly delineate examples with a newline,
												//as stated in the VW documentation.

				}
				
				//TODO: these should go in a finally block
				vwWriter.flush();
				socket.shutdownOutput(); 	//indicate to VW that all data has been sent.
											//cannot call vwWriter.close() because that ends up closing the socket as well.
				
				LOGGER.info("All examples submitted to VW!");
				
			} catch (IOException e) {
				
				LOGGER.error("Exception in VWExampleSubmitter: " + e.getMessage(), e);
			}
			
			//the prediction iterator will shut down the socket.
			//can't call vwWriter.close() here because that will shut the socket down, but we can't do that
			//at this point because the prediction reader might still be reading from it.
			
		}
		
	}
	
	private static class VWPredictionIterable implements Iterable<String>
	{
		private Socket socket;
		
		private static final Logger LOGGER = LoggerFactory.getLogger(VWExampleSubmitter.class);
		
		public VWPredictionIterable(Socket socket)
		{
			this.socket = socket;
		}
		
		@Override
		public Iterator<String> iterator() {
			
			try {
				return new VWPredictionIterator(socket);
			} catch (IOException e) {
				LOGGER.error("Error in VWPredictionIterable: " + e.getMessage(), e);
			}
			
			return new ArrayList<String>().iterator();
		}
		
	}
	
	/*
	 * Reads predictions from VW in a separate thread.
	 */
	private static class VWPredictionIterator implements Iterator<String>
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(VWPredictionIterator.class);
		
		private Socket socket;
		
		private BufferedReader vwReader;
		
		private String nextLineToReturn = null;
		
		public VWPredictionIterator(Socket socket) throws IOException
		{
			this.socket = socket;
			
			vwReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			advance();
		}

		@Override
		public boolean hasNext() {

			return nextLineToReturn != null;
		}

		@Override
		public String next() {
			String toReturn = nextLineToReturn;
			
			advance();
			
			return toReturn;
		}

		@Override
		public void remove() {
			
			throw new UnsupportedOperationException("VWPredictionIterator doesn't support the 'remove' operation!");
			
		}

		private void advance()
		{
			boolean closeReader = false;
			try {
				nextLineToReturn = vwReader.readLine();
				
				closeReader = nextLineToReturn == null;
				
			} catch (Exception e) {
				LOGGER.error("Error in VWPredictionIterator: " + e.getMessage(), e);
				closeReader = true;
			}
			finally
			{
				if (closeReader)
				{
					try {
						vwReader.close();
					} catch (Exception e2) {
						LOGGER.warn("Failed to close the reader in VWPredictionIterator: " + e2.getMessage(), e2);
					}
					
					if (socket.isClosed() == false)
						try {
							socket.close();
						} catch (Exception e2) {
							LOGGER.warn("Failed to close the socket in VWPredictionIterator: " + e2.getMessage(), e2);
						}

				}
			}
		}
	}

}
