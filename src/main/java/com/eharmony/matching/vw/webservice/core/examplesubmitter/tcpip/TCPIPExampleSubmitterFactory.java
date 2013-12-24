/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.examplesubmitter.tcpip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;

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

	private final TCPIPSocketFactory socketFactory;

	/*
	 * An application wide thread pool service.
	 */
	private final ExecutorService executorService;

	@Autowired
	public TCPIPExampleSubmitterFactory(TCPIPSocketFactory socketFactory,
			@Value("#{executorService}") ExecutorService executorService) {

		checkNotNull(socketFactory, "A null socket factory cannot be provided!");
		checkNotNull(executorService,
				"A null executor service cannot be provided!");

		this.socketFactory = socketFactory;
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
		// examples iterable by examining its attributes.

		// returning the TCP IP async submitter for now.
		return new AsyncFailFastTCPIPExampleSubmitter(socketFactory,
				executorService, theExamples);
	}

}
