/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

/**
 * @author vrahimtoola Tests the web service using the Jersey Apache Client.
 */
public class JerseyApacheClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JerseyApacheClientTest.class);

	@Ignore
	@Test
	public void test() throws IOException, InterruptedException {

		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));

		PipedOutputStream exampleOutputStream = new PipedOutputStream();
		final BufferedWriter exampleWriter = new BufferedWriter(new OutputStreamWriter(exampleOutputStream));

		PipedInputStream exampleInputStream = new PipedInputStream(exampleOutputStream);

		String theExample = "1 | w_2=German pre1_2=g c_0=A_fw=y c_0=A c_2=Aa suf2_2=an pre2_2=ge c_2=Aa_fw=n w_-1=<s> suf3_0=u suf1_0=u suf2_1=ts pre3_1=rej c_1=a w_1=rejects suf2_0=eu pre2_1=re suf3_1=cts suf3_2=man w_0=EU pre1_1=r pre1_0=e c_1=a_fw=n w_-2=<s> pre3_2=ger l_2=german l_0=eu pre3_0=eu pre2_0=eu suf1_1=s l_1=rejects suf1_2=n";

		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 0);

		Client client = ApacheHttpClient.create(config);

		//client.asyncResource("").accept("*/*").entity(exampleInputStream, MediaType.TEXT_PLAIN_TYPE)..build(URI.create("http://localhost:8080/vw-webservice/predict/main"), "POST");

		ClientRequest request = ClientRequest.create().accept("*/*").entity(exampleInputStream, MediaType.TEXT_PLAIN_TYPE).build(URI.create("http://localhost:8080/vw-webservice/predict/main"), "POST");

		final CountDownLatch okToSendRequest = new CountDownLatch(1);

		new Thread(new Runnable() {

			@Override
			public void run() {

				okToSendRequest.countDown();

				String exampleIn = null;

				try {

					long numExamplesSent = 0;

					while ((exampleIn = testReader.readLine()) != null) {
						exampleWriter.write(exampleIn);
						exampleWriter.write("\n");

						numExamplesSent++;

						if (numExamplesSent == 5000) break;
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					exampleWriter.flush();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					exampleWriter.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		okToSendRequest.await();

		ClientResponse response = client.handle(request);

		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntityInputStream()));

		String prediction = null;

		long numPredictions = 0;

		while ((prediction = reader.readLine()) != null) {
			LOGGER.debug("Got prediction: {}", prediction);

			numPredictions++;
		}

		LOGGER.debug("Read a total of {} predictions", numPredictions);

	}
}
