package com.eharmony.matching.vw.webservice;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.core.ExamplesIterable;

/**
 * Root resource (exposed at "predict" path)
 */
@Component
@Path("/predict")
public class PredictResource {

	private final RequestHandlerFactory requestHandlerFactory;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PredictResource.class);

	@Autowired
	public PredictResource(RequestHandlerFactory requestHandlerFactory) {

		checkNotNull(requestHandlerFactory,
				"A request handler factory must be provided!");

		this.requestHandlerFactory = requestHandlerFactory;
	}

	@POST
	@Consumes({ ExampleMediaTypes.PLAINTEXT_1_0, MediaType.TEXT_PLAIN })
	@Produces({ /* PredictionMediaTypes.PLAINTEXT_1_0 */MediaType.TEXT_PLAIN })
	public void doPredict(ExamplesIterable examplesIterable,
			@Suspended final AsyncResponse asyncResponse) throws IOException {

		requestHandlerFactory.getRequestHandler().handleRequest(
				examplesIterable, asyncResponse);
	}

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String doGet() {

		return "Hello from the VW Predict web service!"; // TODO: spit out usage
															// instructions
															// here, perhaps?
	// TODO: alternatively, return an actual form that lets you
	// submit/upload examples
	}
}
