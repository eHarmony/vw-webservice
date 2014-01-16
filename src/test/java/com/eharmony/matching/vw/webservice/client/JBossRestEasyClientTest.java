/**
 * 
 */
package com.eharmony.matching.vw.webservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author vrahimtoola Tests the web service using JBoss' RestEasy client.
 */
public class JBossRestEasyClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JBossRestEasyClientTest.class);

	@Test
	public void test() throws IOException {
		//the examples
		GZIPInputStream gzipInputStream = new GZIPInputStream(this.getClass().getClassLoader().getResourceAsStream("ner.train.gz"));
		final BufferedReader testReader = new BufferedReader(new InputStreamReader(gzipInputStream, Charsets.UTF_8));
		//
		//		ResteasyClient client = new ResteasyClientBuilder().build();
		//        ResteasyWebTarget target = client.target("http://foo.com/resource");

	}

}
