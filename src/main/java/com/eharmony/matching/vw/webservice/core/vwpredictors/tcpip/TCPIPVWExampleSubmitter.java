/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.vwpredictors.tcpip;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.core.VWExampleSubmissionException;
import com.eharmony.matching.vw.webservice.core.vwexample.VWExample;

/**
 * @author vrahimtoola
 * 
 *         Submits VW examples to VW running as a daemon, over a TCP-IP socket
 *         connection.
 */
public class TCPIPVWExampleSubmitter implements Callable<Void> {
	private final Socket socket;
	private final Iterable<VWExample> vwExamples;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TCPIPVWExampleSubmitter.class);

	public TCPIPVWExampleSubmitter(Socket socket, Iterable<VWExample> vwExamples) {
		this.socket = socket;
		this.vwExamples = vwExamples;
	}

	@Override
	public Void call() throws VWExampleSubmissionException {

		OutputStream outputStream;

		try {

			outputStream = socket.getOutputStream();

			LOGGER.info("Starting to submit examples to VW...");

			for (VWExample example : vwExamples) {

				example.write(outputStream);
			}

			LOGGER.info("All examples submitted to VW!");

		} catch (IOException e) {

			LOGGER.error("Exception in VWExampleSubmitter: {}", e.getMessage(),
					e);

			throw new VWExampleSubmissionException(
					"Exception when sending examples to VW!", e);

		} finally {

			try {
				socket.shutdownOutput();
			} catch (IOException e2) {
				throw new VWExampleSubmissionException(
						"Exception when closing connection to VW after sending examples - possible that not all examples might've been submitted!",
						e2);
			}

		}

		return null;

	}

}
