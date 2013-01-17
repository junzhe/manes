package org.whispercomm.manes.topology.server.http;

import static org.junit.Assert.*;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.HttpUnitTestMain;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TopologyHttpUnitTest {
	HttpUnitTestMain main;

	@Before
	public void setUp() throws Exception{
		main = new HttpUnitTestMain();
		main.start();
		if (!main.await())
			throw new RuntimeException(
					"Failed to start HttpUnitTestMain server.");
	}
	
	// TODO should add OAuth to this test
//	@Test
//	public void locationTest(){
//		Client client = Client.create();
//
//		WebResource webResource = client
//				.resource("http://localhost:7890/user/0/location/");
//		Location location = Utility.initLocationInstance();
//		JsonConfig jsonConfig = new JsonConfig();  
//		jsonConfig.setRootClass(Location.class);
//		JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
//		ClientResponse response = webResource.type("application/json")
//				.post(ClientResponse.class, json.toString());
//		assertTrue(response.getStatus()==201);
//
//	}
}
