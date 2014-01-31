/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vrahimtoola
 * 
 *         A basic implementation of the TCPIPSocketFactory interface.
 */
public class TCPIPSocketFactoryImpl implements TCPIPSocketFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(TCPIPSocketFactoryImpl.class);

	private final String vwHost;
	private final int vwPort;

	public TCPIPSocketFactoryImpl(String vwHost, int vwPort) {

		checkArgument(StringUtils.isBlank(vwHost) == false, "The hostname for VW must be provided!");
		checkArgument(vwPort > 0, "Invalid port specified for VW!");

		this.vwHost = vwHost;
		this.vwPort = vwPort;
	}

	public Socket getSocket() throws UnknownHostException, IOException {

		LOGGER.debug("Returning socket for host: {} and port: {}", vwHost, vwPort);

		return new Socket(vwHost, vwPort);
	}

}
