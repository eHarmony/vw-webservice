package com.eharmony.matching.vw.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ServiceLoader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.core.VWPredictor;

/**
 * Root resource (exposed at "predict" path)
 */
@Component
@Path("/predict")
public class PredictResource {

	private VWPredictor vwPredictor;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StringIterableMessageBodyReader.class);
	
	@Autowired
	public PredictResource(VWPredictor vwPredictor)
	{
		if (vwPredictor == null)
			throw new IllegalArgumentException("A VWPredictor must be provided!");
		
		this.vwPredictor = vwPredictor;
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPredict(final Iterable<String> vwExamples) throws IOException
	{
		//TODO: look into coda hale metrics library, consider JMX 
		
		StreamingOutput output = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {

				Iterable<String> predictionsIterable = vwPredictor.predict(vwExamples);
				
				if (predictionsIterable == null)
					throw new WebApplicationException("A null predictions iterable was returned!");
				
				Charset charset = ReaderWriter.getCharset(MediaType.TEXT_PLAIN_TYPE);
				
				byte[] newlineBytes = System.getProperty("line.separator").getBytes(charset);
				
				for (String prediction : predictionsIterable)
				{
					LOGGER.info("Writing output: " + prediction.getBytes(charset));
					
					output.write(prediction.getBytes(charset));
					output.write(newlineBytes);
				}
				
				output.flush();	
			}
		};
		
		return Response.ok(output).build();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String doGet() {
    	
    	ServiceLoader<MessageBodyReader> tempLoader = ServiceLoader.load(MessageBodyReader.class);
    	
    	String loadedServices = "";
    	
    	if (tempLoader != null)
    	{
    		for (MessageBodyReader<String> mbBodyReader : tempLoader)
    		{
    			LOGGER.info("*** Loaded service: " + mbBodyReader.getClass());
    		}
    		
    		loadedServices = "loader services";
    	}
    	else {
			
    		LOGGER.info("**** failed to load services!");
    		loadedServices = "no services loaded";
		}
    	
        return "Hello from the VW Predict web service! - " + loadedServices; //TODO: spit out usage instructions here, perhaps?
    }
}
