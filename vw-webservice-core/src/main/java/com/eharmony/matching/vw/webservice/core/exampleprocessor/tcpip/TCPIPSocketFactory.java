/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author vrahimtoola
 * 
 *         Returns a TCP IP socket that can be used for communicating with a VW
 *         daemon. This abstraction has been added to facilitate testing of the
 *         TCP IP example submitters and prediction fetchers.
 */
public interface TCPIPSocketFactory {

	/*
	 * Returns a socket connection to a running VW daemon.
	 * 
	 * @returns A TCP IP socket that can be used for communicating with a VW
	 * daemon. Note that the caller owns this socket and is responsible for any
	 * cleanup (ie, shutting it down when done).
	 */
	Socket getSocket() throws UnknownHostException, IOException;
}
