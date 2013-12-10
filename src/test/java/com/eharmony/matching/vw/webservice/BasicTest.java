/**
 * 
 */
package com.eharmony.matching.vw.webservice;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ChunkedInput;
import org.junit.Test;


/**
 * @author vrahimtoola
 *
 */
public class BasicTest {

	@Test
	public void test() {
//		Client client = ClientBuilder.newClient();
//		WebTarget target = client.target("http://lp-prod1.dc1.eharmony.com:8080/vw-webservice").path("predict");
//		 
//		Form form = new Form();
//		form.param("x", "foo");
//		form.param("y", "bar");
//		
//		Response response = target.request(MediaType.TEXT_PLAIN).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
//		
//		final ChunkedInput<String> chunkedInput =
//		        response.readEntity(new GenericType<ChunkedInput<String>>() {});
//		
//		chunkedInput.setParser(ChunkedInput.createParser("\r\n".getBytes()));
//		
//		String chunk;
//		while ((chunk = chunkedInput.read()) != null) {
//		    System.out.println("Next chunk received: " + chunk);
//		}
	}

}
