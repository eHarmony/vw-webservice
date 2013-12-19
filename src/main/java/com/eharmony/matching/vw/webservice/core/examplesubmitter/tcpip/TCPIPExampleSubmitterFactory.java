/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter.tcpip;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitter;
import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFactory;

/**
 * @author vrahimtoola A factory that returns example submitters that submit
 *         examples to VW over a TCP-IP socket.
 */
@Component
public class TCPIPExampleSubmitterFactory implements ExampleSubmitterFactory {

	private final String vwHost;
	private final int vwPort;

	/*
	 * An application wide thread pool service.
	 */
	private final ExecutorService executorService;

	@Autowired
	public TCPIPExampleSubmitterFactory(@Value("${vw.hostName}") String vwHost,
			@Value("${vw.port}") int vwPort,
			@Value("#{executorService}") ExecutorService executorService) {

		checkArgument(StringUtils.isBlank(vwHost) == false,
				"The hostname for VW must be provided!");
		checkArgument(vwPort > 0, "Invalid port specified for VW!");
		checkNotNull(executorService,
				"A null executor service cannot be provided!");

		this.vwHost = vwHost;
		this.vwPort = vwPort;
		this.executorService = executorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.examplesubmitter.
	 * ExampleSubmitterFactory#getExampleSubmitter(java.lang.Iterable,
	 * java.util.EnumSet)
	 */
	@Override
	public ExampleSubmitter getExampleSubmitter(ExamplesIterable theExamples) {

		// TODO: return a proper example submitter based on the provided
		// options.

		// returning the TCP IP async submitter for now.
		return new AsyncFailFastTCPIPExampleSubmitter(vwHost, vwPort,
				executorService, theExamples);
	}

}
