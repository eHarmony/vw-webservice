/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola
 * 
 */
public class JerseyClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JerseyClientTest.class);

	//@Ignore
	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.connectorProvider(new GrizzlyConnectorProvider()); //must use this, HttpUrlConnector doesn't seem to handle chunked encoding too well.

		Client client = ClientBuilder.newClient(clientConfig);

		client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 2048);
		client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);

		WebTarget target = client.target("http://localhost:8080").path("/vw-webservice/predict/main");

		Future<Response> future = target.request("*/*").async().post(Entity.text(gzipInputStream));

		Response response = future.get();

		final ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<ChunkedInput<String>>() {
		});
		chunkedInput.setParser(ChunkedInput.createParser("\n"));
		String chunk;
		LOGGER.debug("starting to read responses...");

		while ((chunk = chunkedInput.read()) != null) {
			System.out.println("Next chunk received: " + chunk);
		}

	}

}
