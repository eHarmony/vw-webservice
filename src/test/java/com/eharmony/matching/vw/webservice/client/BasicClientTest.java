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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eharmony.matching.vw.webservice.ExampleMediaTypes;
import com.eharmony.matching.vw.webservice.core.prediction.Prediction;
import com.eharmony.matching.vw.webservice.messagebodyreader.plaintextpredictionsmessagebodyreader.PlainTextPredictionsMessageBodyReader;

/**
 * @author vrahimtoola A basic client that connects to a running instance of the
 *         VW webservice.
 */
public class BasicClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasicClientTest.class);

	@Ignore
	@Test
	public void simpleTest() throws IOException, InterruptedException, ExecutionException {
		//change these accordingly
		final String hostAndPort = "http://vw-webservice.np.dc1.eharmony.com:8080/vw-webservice";

		Client client = ClientBuilder.newClient();
		client.register(PlainTextPredictionsMessageBodyReader.class);
		WebTarget target = client.target(hostAndPort).path("predict");

		//prepare the data to send over to
		final GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));

		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream));

		//add appropriate headers
		Invocation invocation = target.request(MediaType.TEXT_PLAIN).header("Content-Type", MediaType.TEXT_PLAIN).buildPost(Entity.entity(new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {

				String line = null;

				String newLine = System.getProperty("line.separator");

				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

				long numExamplesSent = 0;

				while ((line = testReader.readLine()) != null) {

					writer.write(line);
					writer.write(newLine);

					//LOGGER.info("Sent {} examples", numExamplesSent++);

					if (++numExamplesSent > 10000) break;
				}

				writer.flush();

			}
		}, ExampleMediaTypes.PLAINTEXT_1_0));

		Future<Iterable> future = invocation.submit(Iterable.class);

		LOGGER.info("got a response!");
		;

		Iterable<Prediction> predictions = future.get();

		//		Iterable<Prediction> predictions = response.readEntity(Iterable.class);
		//
		for (Prediction p : predictions) {

			LOGGER.info("Received prediction: {}", p.getVWStringRepresentation());
		}
	}
}
