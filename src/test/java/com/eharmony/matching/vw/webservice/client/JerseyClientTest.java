/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import junit.framework.Assert;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.example.ProtoBufStringWrapperVWExample;
import com.google.common.base.Charsets;

/**
 * @author vrahimtoola
 * 
 *         This is the only client that actually works! Need to use the Grizzly
 *         Connector Provider tho otherwise it won't work.
 */
public class JerseyClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JerseyClientTest.class);

	@Ignore
	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.connectorProvider(new GrizzlyConnectorProvider()); //must use this, HttpUrlConnector doesn't seem to handle chunked encoding too well.

		Client client = ClientBuilder.newClient(clientConfig);

		//		client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 2048);
		//		client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);

		WebTarget target = client.target("http://localhost:8080").path("/vw-webservice/predict/main");

		Future<Response> future = target.request("*/*").async().post(Entity.text(gzipInputStream));

		Response response = future.get();

		final ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<ChunkedInput<String>>() {
		});
		chunkedInput.setParser(ChunkedInput.createParser("\n"));
		String chunk;
		LOGGER.debug("starting to read responses...");

		while ((chunk = chunkedInput.read()) != null) {
			LOGGER.debug("Next chunk received: " + chunk);
		}

	}

	@Ignore
	@Test
	public void simpleStringWrappingProtoBufExamplesTest() throws IOException, InterruptedException, ExecutionException {
		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.connectorProvider(new GrizzlyConnectorProvider()); //must use this, HttpUrlConnector doesn't seem to handle chunked encoding too well.

		Client client = ClientBuilder.newClient(clientConfig);

		final PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

		final CountDownLatch startWritingExamplesLatch = new CountDownLatch(1);

		Future<Void> successFuture = Executors.newCachedThreadPool().submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				startWritingExamplesLatch.await();

				String readExample = null;

				while ((readExample = testReader.readLine()) != null) {
					ProtoBufStringWrapperVWExample.StringWrapperExample writtenExample = ProtoBufStringWrapperVWExample.StringWrapperExample.newBuilder().setExampleString(readExample).build();

					writtenExample.writeDelimitedTo(pipedOutputStream);

					pipedOutputStream.flush(); //need to flush right after writing, otherwise the reading thread won't get it 

				}

				pipedOutputStream.flush();
				pipedOutputStream.close();

				return null;
			}

		});

		WebTarget target = client.target("http://localhost:8080").path("/vw-webservice/predict/main");

		Future<Response> future = target.request("*/*").async().post(Entity.entity(pipedInputStream, MediaType.valueOf(ExampleMediaTypes.SIMPLE_PROTOBUF_1_0)));

		startWritingExamplesLatch.countDown();

		Response response = future.get();

		final ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<ChunkedInput<String>>() {
		});
		chunkedInput.setParser(ChunkedInput.createParser("\n"));
		String chunk;
		LOGGER.debug("starting to read responses...");

		long numExamplesProcessed = 0;

		while ((chunk = chunkedInput.read()) != null) {
			LOGGER.debug("Next chunk received: " + chunk);
			numExamplesProcessed++;
		}

		Assert.assertEquals(272274, numExamplesProcessed);

		successFuture.get(); //verify that no exception was thrown
	}

	/*
	 * Same as the above test but submits the set of examples 100 times (for a
	 * total of about 27 million examples).
	 */
	@Ignore
	@Test
	public void hugeTest() throws IOException, InterruptedException, ExecutionException {

		final ClientConfig clientConfig = new ClientConfig();
		//clientConfig.property(ClientProperties.CHUNKED_ENCODING_SIZE, 2048);
		clientConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);
		clientConfig.property(ClientProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, Integer.valueOf(-1));
		clientConfig.register(ExampleMessageBodyWriter.class);

		clientConfig.connectorProvider(new GrizzlyConnectorProvider() {

			@Override
			public Connector getConnector(Client client, Configuration config) {

				LOGGER.debug("Get connector called!");

				config = clientConfig;

				for (Map.Entry<String, Object> entry : config.getProperties().entrySet()) {

					LOGGER.debug("{}:{}", entry.getKey(), entry.getValue());
				}

				return super.getConnector(client, config);

			}

		}); //must use this, HttpUrlConnector doesn't seem to handle chunked encoding too well.

		Client client = ClientBuilder.newClient(clientConfig);

		WebTarget target = client.target("http://localhost:8080").path("/vw-webservice/predict/main");

		LOGGER.debug("About to submit request...");

		Future<Response> future = target.request("*/*").async().post(Entity.entity(new ExampleClass(), MediaType.TEXT_PLAIN));

		LOGGER.debug("Req submitted...");

		Response response = future.get();

		final ChunkedInput<String> chunkedInput = response.readEntity(new GenericType<ChunkedInput<String>>() {
		});
		chunkedInput.setParser(ChunkedInput.createParser("\n"));
		String chunk;
		LOGGER.debug("starting to read responses...");

		while ((chunk = chunkedInput.read()) != null) {
			//System.out.println("Next chunk received: " + chunk);
		}

	}

	class ExampleClass {
	};

	@Produces({ MediaType.TEXT_PLAIN })
	@Provider
	static class ExampleMessageBodyWriter implements MessageBodyWriter<ExampleClass> {

		private static Logger LOGGER = LoggerFactory.getLogger(ExampleMessageBodyWriter.class);

		@Override
		public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			return true;
		}

		@Override
		public long getSize(ExampleClass t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			return -1;
		}

		@Override
		public void writeTo(ExampleClass t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

			LOGGER.debug("Writing to a stream of type: {}", entityStream.getClass());

			try {

				boolean limited = false;
				long limit = 5;

				BufferedWriter exampleWriter = new BufferedWriter(new OutputStreamWriter(entityStream));

				for (int numTimes = 0; numTimes < 100; numTimes++) {
					//the examples
					GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

					BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

					String example = null;

					long numExamplesSubmitted = 0;

					while ((example = testReader.readLine()) != null) {
						exampleWriter.write(example);
						exampleWriter.newLine();

						numExamplesSubmitted++;

						if (limited && numExamplesSubmitted == limit) break;
					}

					testReader.close();

					exampleWriter.flush();

					entityStream.flush();
				}

				exampleWriter.close();

			}
			catch (Exception e) {
				LOGGER.error("Error in message body writer! {}", e.getMessage(), e);
			}
		}

	}

}
