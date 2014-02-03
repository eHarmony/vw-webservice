/**
 * 
 */
package com.eharmony.matching.vw.webservice.core.exampleprocessor.tcpip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;

import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessor;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFactory;

/**
 * @author vrahimtoola
 * 
 *         A factory that returns example processors that submit examples to VW
 *         over a TCP-IP socket and read predictions back the same way.
 */
public class TCPIPExampleProcessorFactory implements ExampleProcessorFactory {

	private final TCPIPSocketFactory socketFactory;

	/*
	 * An application wide thread pool service.
	 */
	private final ExecutorService executorService;

	public TCPIPExampleProcessorFactory(TCPIPSocketFactory socketFactory,
	ExecutorService executorService) {

		checkNotNull(socketFactory, "A null socket factory cannot be provided!");
		checkNotNull(executorService, "A null executor service cannot be provided!");

		this.socketFactory = socketFactory;
		this.executorService = executorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eharmony.matching.vw.webservice.core.exampleprocessor.
	 * ExampleProcessorFactory#getExampleSubmitter(java.lang.Iterable,
	 * java.util.EnumSet)
	 */
	public ExampleProcessor getExampleProcessor(ExamplesIterable theExamples) {

		// TODO: return a proper example submitter based on the provided
		// examples iterable by examining its attributes.

		// returning the TCP IP async submitter for now.
		return new AsyncFailFastTCPIPExampleProcessor(socketFactory, executorService, theExamples);
	}

}
