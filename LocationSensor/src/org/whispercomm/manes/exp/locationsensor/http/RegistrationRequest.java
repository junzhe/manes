package org.whispercomm.manes.exp.locationsensor.http;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispercomm.manes.exp.locationsensor.network.ManesService;

import android.util.Log;

/**
 * Wrapper class for sending registration requests to the MANES server
 * 
 * @author David Adrian
 * 
 */
public final class RegistrationRequest extends HttpPost {

	private static final String TAG = RegistrationRequest.class.getName();

	// JSON Keys
	public static final String KEY_SECRET = "secret";

	private final String secret;

	public RegistrationRequest(String secret) throws ManesHttpException {
		super(ManesService.SERVER_URL + "/user/");
		this.secret = secret;
		this.prepareRequest();
	}

	private void prepareRequest() throws ManesHttpException {
		JSONObject data = new JSONObject();
		try {
			// Create the JSON object
			data.put(KEY_SECRET, this.secret);
			Log.d(TAG, data.toString());

			// Convert the JSON object to an HttpEntity and hand it to the
			// request
			this.setHeader("Content-Type", "application/json");
			this.setEntity(new StringEntity(data.toString()));
		} catch (JSONException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		}
	}

}
