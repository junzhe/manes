package org.whispercomm.manes.client.macentity.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.test.MacEntityTestRunner;

@RunWith(MacEntityTestRunner.class)
public class SendTest {

	
	private static final String JSON_KEY_CONTENTS = "contents";
	private static final String JSON_KEY_APP = "app_id";
	private static final String userId = "18";
	private static final int appId = 1;
	private SendRequest request;
	private byte[] contents;
	
	@Before
	public void setUp() {
		contents = ManesTestUtility.generatePacketAsBytes(32);
	}
	
	@After
	public void takeDown() {
		contents = null;
		request = null;
	}
	
	@Test
	public void testCreation() {
		try {
			request = new SendRequest(userId, appId, contents);
			
			// Check that we set this request as having JSON
			Header headers[] = request.getHeaders("content-type");
			assertTrue(headers.length == 1);
			assertEquals(headers[0].getValue(), "application/json");
			
			// Check that there is some string with the contents key in the request
			HttpEntity entity = request.getEntity();
			String jString = HttpManager.ReadResponseEntity(entity);
			JSONObject jObject = new JSONObject(jString);
			assertNotNull(jObject.getString(JSON_KEY_CONTENTS));
			assertEquals(jObject.getString(JSON_KEY_APP), Integer.toString(appId));
		} catch (ManesHttpException e) {
			fail("Exception thrown: " + e.getMessage());
		} catch (IOException e) {
			fail("ReadResponseEntity failed: " + e.getMessage());
		} catch (JSONException e) {
			fail("Invalid JSON in request: " + e.getMessage());
		}
	}
}
