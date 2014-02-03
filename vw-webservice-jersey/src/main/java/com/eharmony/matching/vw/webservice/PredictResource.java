package com.eharmony.matching.vw.webservice;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.common.example.Example;
import com.eharmony.matching.vw.webservice.common.example.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.common.prediction.PredictionMediaTypes;
import com.eharmony.matching.vw.webservice.core.ExamplesIterable;
import com.eharmony.matching.vw.webservice.core.exampleprocessor.ExampleProcessorFactory;

/**
 * Root resource (exposed at "predict" path)
 */
@Component
@Path("/predict")
public class PredictResource {

	private final ExampleProcessorFactory exampleProcessorFactory;

	private final ExecutorService executorService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PredictResource.class);

	@Autowired
	public PredictResource(ExecutorService executorService, ExampleProcessorFactory exampleProcessorFactory) {

		checkNotNull(exampleProcessorFactory, "An example processor factory must be provided!");

		this.exampleProcessorFactory = exampleProcessorFactory;

		this.executorService = executorService;

	}

	@POST
	@Consumes({ ExampleMediaTypes.PLAINTEXT_1_0, MediaType.TEXT_PLAIN, ExampleMediaTypes.SIMPLE_PROTOBUF_1_0, ExampleMediaTypes.SIMPLE_JSON_1_0, ExampleMediaTypes.STRUCTURED_JSON_1_0 })
	@Produces({ PredictionMediaTypes.PLAINTEXT_1_0 })
	@Path("/main")
	public ChunkedOutput<String> doPredict(ExamplesIterable examplesIterable) throws IOException {

		return new RequestHandler(executorService, exampleProcessorFactory).handleRequest(examplesIterable);
	}

	@POST
	@Consumes({ ExampleMediaTypes.PLAINTEXT_1_0, MediaType.TEXT_PLAIN, ExampleMediaTypes.STRUCTURED_JSON_1_0 })
	@Produces({ PredictionMediaTypes.PLAINTEXT_1_0 })
	@Path("/dumpExamples")
	public String dumpExamples(ExamplesIterable examplesIterable) {
		if (LOGGER.isTraceEnabled()) for (Example example : examplesIterable)
			LOGGER.trace(example.getVWStringRepresentation());

		return "Done!";
	}

	@POST
	@Consumes({ ExampleMediaTypes.PLAINTEXT_1_0, MediaType.TEXT_PLAIN })
	@Produces({ PredictionMediaTypes.PLAINTEXT_1_0 })
	@Path("/dumpExamplesNew")
	public String dumpExamplesNew(String content) {

		return content;
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
