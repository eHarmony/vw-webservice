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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author vrahimtoola
 * 
 *         A basic implementation of the TCPIPSocketFactory interface.
 */
@Component
public class TCPIPSocketFactoryImpl implements TCPIPSocketFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(TCPIPSocketFactoryImpl.class);

	private final String vwHost;
	private final int vwPort;

	@Autowired
	public TCPIPSocketFactoryImpl(@Value("${vw.hostName}") String vwHost,
			@Value("${vw.port}") int vwPort) {

		checkArgument(StringUtils.isBlank(vwHost) == false, "The hostname for VW must be provided!");
		checkArgument(vwPort > 0, "Invalid port specified for VW!");

		this.vwHost = vwHost;
		this.vwPort = vwPort;
	}

	@Override
	public Socket getSocket() throws UnknownHostException, IOException {

		LOGGER.debug("Returning socket for host: {} and port: {}", vwHost, vwPort);

		return new Socket(vwHost, vwPort);
	}

}
