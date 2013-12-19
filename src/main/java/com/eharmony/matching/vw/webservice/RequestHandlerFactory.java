/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.core.examplesubmitter.ExampleSubmitterFactory;

/**
 * @author vrahimtoola
 * 
 *         Returns request handlers to handle individual requests.
 */
@Component
public class RequestHandlerFactory {

	private final ExampleSubmitterFactory exampleSubmitterFactory;

	@Autowired
	public RequestHandlerFactory(ExampleSubmitterFactory exampleSubmitterFactory) {
		checkNotNull(exampleSubmitterFactory);

		this.exampleSubmitterFactory = exampleSubmitterFactory;
	}

	public RequestHandler getRequestHandler() {
		return new RequestHandler(exampleSubmitterFactory);
	}
}
