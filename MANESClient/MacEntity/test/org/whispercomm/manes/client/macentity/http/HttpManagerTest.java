package org.whispercomm.manes.client.macentity.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HttpManagerTest {

	private static final String KEY_USER_ID = "user_id";
	private static final String KEY_CONTENTS = "contents";
	private HttpEntity httpEntity;
	private JSONObject packet;
	private JSONObject user;
	private JSONArray jArray;

	@Before
	public void setUp() {
		packet = ManesTestUtility.generateJSONObject(KEY_CONTENTS,
				"someContents");
		user = ManesTestUtility.generateJSONObject(KEY_USER_ID, "0");
		jArray = ManesTestUtility.generateJSONObjectArray(packet, user);
	}

	@After
	public void takeDown() {
		packet = null;
		user = null;
		jArray = null;
	}

	@Test
	public void testReadResponseEntity() {
		try {
			httpEntity = new StringEntity(user.toString());
			String jData = HttpManager.ReadResponseEntity(httpEntity);
			JSONObject jObject = new JSONObject(jData);
			assertEquals(jObject.getString(KEY_USER_ID), "0");
			
			httpEntity = new StringEntity(packet.toString());
			jData = HttpManager.ReadResponseEntity(httpEntity);
			jObject = new JSONObject(jData);
			assertEquals(jObject.getString(KEY_CONTENTS), "someContents");
			
			httpEntity = new StringEntity(jArray.toString());
			jData = HttpManager.ReadResponseEntity(httpEntity);
			JSONArray extractedArray = new JSONArray(jData);
			
			JSONObject first = extractedArray.getJSONObject(0);
			JSONObject second = extractedArray.getJSONObject(1);
			assertEquals(first.getString(KEY_CONTENTS), packet.getString(KEY_CONTENTS));
			assertEquals(second.getString(KEY_USER_ID), user.getString(KEY_USER_ID));
		} catch (IllegalStateException e) {
			fail("Caught Illegal State Exception");
		} catch (IOException e) {
			fail("Caught IOException");
		} catch (JSONException e) {
			fail("Could not reconstruct JSONObject from data string");
		}
	}

	public void testCheckThreadSafeConnectionManager() {
		HttpParams httpParams = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager connManager = new SingleClientConnManager(
				httpParams, schemeRegistry);
		HttpClient customHttpClient = new DefaultHttpClient(connManager,
				httpParams);
		try {
			@SuppressWarnings("unused")
			HttpManager httpManager = new HttpManager(customHttpClient);
		} catch (ManesHttpException e) {
			// Should catch exception
		}
		fail("Constructor allowed use of non-thread safe connection manager");
	}
}
