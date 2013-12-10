package com.eharmony.matching.vw.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eharmony.matching.vw.webservice.core.VWPredictor;
import com.eharmony.matching.vw.webservice.util.StringIterable;

/**
 * Root resource (exposed at "predict" path)
 */
@Component
@Path("/predict")
public class PredictResource {

	private VWPredictor vwPredictor;
	
	@Autowired
	public PredictResource(VWPredictor vwPredictor)
	{
		if (vwPredictor == null)
			throw new IllegalArgumentException("A VWPredictor must be provided!");
		
		this.vwPredictor = vwPredictor;
	}

	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String doPredict(
							@DefaultValue("")
							@FormParam(value = "vwExamples") String vwExamples) throws IOException
	{
		
		if (StringUtils.isBlank(vwExamples))
			return "";

		Iterable<String> predictions = vwPredictor.predict(getIterableFromExampleString(vwExamples));

		StringBuilder builder = new StringBuilder();
		
		for (String prediction : predictions)
			builder.append(prediction + System.getProperty("line.separator"));
		
		return builder.toString();
	}
	
	private Iterable<String> getIterableFromExampleString(String vwExamples) throws IOException
	{
		return new StringIterable(vwExamples);
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
        return "Hello from the VW Predict web service!"; //TODO: spit out usage instructions here, perhaps?
    }
}
