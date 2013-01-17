package org.whispercomm.manes.client.macentity.http;

import static org.junit.Assert.fail;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Static utility functions to be used when unit testing the MANES client
 * 
 * @author David Adrian
 * 
 */
public class ManesTestUtility {

	/**
	 * Make a JSON Object
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static JSONObject generateJSONObject(String key, String value) {
		JSONObject jObject;
		try {
			jObject = new JSONObject().put(key, value);
			return jObject;
		} catch (JSONException e) {
			fail("Unable to create JSONObject for test");
			return null;
		}
	}

	/**
	 * Make a JSONArray of JSONObjects
	 * 
	 * @param args
	 *            Some JSONObjects
	 * @return
	 */
	public static JSONArray generateJSONObjectArray(JSONObject... args) {
		if (args.length == 0) {
			return null;
		}
		JSONArray jArray = new JSONArray();
		for (JSONObject jObject : args) {
			jArray.put(jObject);
		}
		return jArray;
	}

	/**
	 * Generate a pseudo-random {@code byte[]} for testing.
	 * 
	 * @param size
	 * @return {@code byte[]} that can be used a packet
	 */
	public static byte[] generatePacketAsBytes(int size) {
		if (size > 1500 || size < 1) {
			fail("Test writer is asking too much!");
		}

		byte[] packet = new byte[size];
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			int next = random.nextInt();
			packet[i] = (byte) (next & 0xFF);
		}
		return packet;

	}
}
