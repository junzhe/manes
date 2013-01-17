package org.whispercomm.manes.client.macentity.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RegistrationTest {

	private RegistrationRequest request;
	private static final String SECRET = "mySecret";
	
	@After 
	public void takeDown() {
		request = null;
	}
	
	@Test 
	public void testCreation() {
		try {
			// Check that the request has the correct headers for typing
			request = new RegistrationRequest(SECRET);
			Header headers[] = request.getHeaders("content-type");
			assertTrue(headers.length == 1);
			assertEquals(headers[0].getValue(), "application/json");
			
			// Check the secret is in the request
			HttpEntity entity = request.getEntity();
			String jString = HttpManager.ReadResponseEntity(entity);
			JSONObject jObject = new JSONObject(jString);
			assertEquals(jObject.get(RegistrationRequest.KEY_SECRET), SECRET);
		} catch (ManesHttpException e) {
			fail("Exception thrown: " + e.getMessage());
		} catch (IOException e) {
			fail("HttpManager.ReadResponseEntity failed: " + e.getMessage());
		} catch (JSONException e) {
			fail("Exception thrown: " + e.getMessage());
		}	
	}
}
