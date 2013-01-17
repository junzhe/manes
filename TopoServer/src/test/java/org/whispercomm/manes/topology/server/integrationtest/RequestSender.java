package org.whispercomm.manes.topology.server.integrationtest;

import static org.junit.Assert.*;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RequestSender {

	static final String SERVER_URL = "http://localhost:7890";
	//static final String SERVER_URL = "http://107.20.243.124:7890";
	
	Client client;

	@Before
	public void setUp() {
		client = Client.create();
	}

	// @Test
	// public void testSendRequest() throws InterruptedException {
	// // new and independent request
	// ClientResponse response = sendNew(1000);
	// assertTrue(response.getStatus() == 201);
	// // request as-prev
	// response = sendAsPrev(1000);
	// assertTrue(response.getStatus() == 201);
	// // request as-prev, when the previous one has expired
	// Thread.sleep(3 * DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD);
	// response = sendAsPrev(1000);
	// assertTrue(response.getStatus() ==
	// TopologyResource.RESPONSE_STATUS_MORE_DETAIL);
	// // new and independent request
	// response = sendNew(1000);
	// assertTrue(response.getStatus() == 201);
	// }

//	@Test
//	public void testTwoUsersGPS() throws InterruptedException {
//		// new and independent request
//		ClientResponse response = sendNew(1000);
//		assertTrue(response.getStatus() == 201);
//		// new and independent request of another user (1001), who lies in the
//		// exact same location.
//		response = sendNewWifiNull(1001);
//		assertTrue(response.getStatus() == 201);
//		// sleep and let the topology expire
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1000);
//		assertTrue(response.getStatus() == 201);
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1001);
//		assertTrue(response.getStatus() == 201);
//	}

//	@Test
//	public void testTwoUsersWifi() throws InterruptedException {
//		// new and independent request
//		ClientResponse response = sendNew(1000);
//		assertTrue(response.getStatus() == 201);
//		// new and independent request of another user (1001), who lies in the
//		// exact same location.
//		response = sendNewGpsNull(1001);
//		assertTrue(response.getStatus() == 201);
//		// sleep and let the topology expire
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1000);
//		assertTrue(response.getStatus() == 201);
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1001);
//		assertTrue(response.getStatus() == 201);
//	}
	
	@Test
	public void testMultipleUsers() throws InterruptedException {
		int user_num = 100;
		ClientResponse response;
		for(int i = 0; i< user_num; i++){
			response = sendNew(1000+i);
			assertTrue(response.getStatus() == 201);
			Thread.sleep(20);
		}
//		// sleep and let the topology expire
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1000);
//		assertTrue(response.getStatus() == 201);
//		Thread.sleep(DataInterfaceVoldemort.LOCATION_UPDATE_PERIOD * 3);
//		response = sendNew(1001);
//		assertTrue(response.getStatus() == 201);
	}
	
	public ClientResponse sendNew(int user_id) {
		WebResource webResource = client.resource(SERVER_URL + "/user/"
				+ user_id + "/location/");
		Location location = Utility.initLocationInstance();
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(Location.class);
		JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, json.toString());

		return response;
	}

	public ClientResponse sendNewWifiNull(int user_id) {
		WebResource webResource = client.resource(SERVER_URL + "/user/"
				+ user_id + "/location/");
		Location location = Utility.initLocationInstanceWifiNull();
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(Location.class);
		JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, json.toString());

		return response;
	}

	public ClientResponse sendNewGpsNull(int user_id) {
		WebResource webResource = client.resource(SERVER_URL + "/user/"
				+ user_id + "/location/");
		Location location = Utility.initLocationInstanceGPSNull();
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(Location.class);
		JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, json.toString());

		return response;
	}
	
	public ClientResponse sendAsPrev(int user_id) {
		WebResource webResource = client.resource(SERVER_URL + "/user/"
				+ user_id + "/location/");
		Location location = Utility.iniLocationInstanceAsPrev();
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(Location.class);
		JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, json.toString());

		return response;
	}
}
