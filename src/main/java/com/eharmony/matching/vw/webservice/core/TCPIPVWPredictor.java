/**
 * 
 */
package com.eharmony.matching.vw.webservice.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

		List<String> toReturn = new ArrayList<String>();
		
		if (vwExamples.iterator().hasNext() == false) //if no examples, don't bother creating the socket connection.
			return toReturn;
		
		Socket socket = null;
		PrintWriter vwWriter = null;
		BufferedReader vwReader = null;
		
		try {
			
			LOGGER.info("Connecting to VW...");
			
			socket = new Socket(hostNameString, port);
			
			vwWriter = new PrintWriter(socket.getOutputStream(), true);
			
			vwReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			for (String example : vwExamples)
			{
				if (StringUtils.isBlank(example))
					continue;
				
				String toSubmit = example.trim() + System.getProperty("line.separator"); 
				
				LOGGER.info("Submitting example to VW: " + toSubmit);
				
				vwWriter.write(toSubmit); 	//need to explicitly delineate examples with a newline,
											//as stated in the VW documentation.

			}
			
			vwWriter.flush();
			socket.shutdownOutput(); 	//indicate to VW that all data has been sent.
										//cannot call vwWriter.close() because that ends up closing the socket as well.

			String readStringFromVW = null;
			while ((readStringFromVW = vwReader.readLine()) != null)
			{
				LOGGER.info("VW returned: " + readStringFromVW);
				
				toReturn.add(readStringFromVW);

			}
			
			LOGGER.info("VW finished returning output, all done here...");

		} catch (Exception e) {
			
			LOGGER.error("Exception during prediction! Exception message: " + e.getMessage(), e);
		}
		finally {
			if (vwReader != null)
			{
				try {
					vwReader.close();
				} catch (Exception e2) {
					LOGGER.warn("Exception closing VW reader! Exception message: " + e2.getMessage(), e2);
				}
			}
			
			if (vwWriter != null)
				try {
					vwWriter.close();
				} catch (Exception e2) {
					LOGGER.warn("Exception closing VW writer! Exception message: " + e2.getMessage(), e2);
				}
			
			if (socket != null && socket.isClosed() == false)
				try {
					socket.close();
				} catch (Exception e2) {
					LOGGER.warn("Exception closing socket connection to VW! Exception message: " + e2.getMessage(), e2);
				}
		}
		
		return toReturn;
	}

}
