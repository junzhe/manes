package org.whispercomm.manes.client.macentity.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * {@link ResponseHandler} for {@link RegistrationRequest}
 * 
 * @author David Adrian
 * 
 */
public final class RegistrationResponseHandler implements ResponseHandler<Long> {

	private static final String TAG = RegistrationResponseHandler.class
			.getName();
	private final static int STATUS_CODE_SUCCESS_REGISTRATION = 201;

	@Override
	public Long handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {

		// Check the status code, log, and return null if its bad
		int statusCode = response.getStatusLine().getStatusCode();
		Log.v(TAG, "Handling server response");
		if (statusCode != STATUS_CODE_SUCCESS_REGISTRATION) {
			Log.d(TAG, "Response: \n status code: " + statusCode
					+ "\n reason phrase: "
					+ response.getStatusLine().getReasonPhrase());
			return null;
		}

		// Status code is good, get the response entity
		HttpEntity entity = response.getEntity();

		// Turn the response entity into a user ID
		try {
			String data = HttpManager.ReadResponseEntity(entity);
			Log.d(TAG, data);
			// Turn the response string to JSON Object
			JSONObject jObj = new JSONObject(data);
			// Extract the user ID
			return Long.valueOf(jObj.getString("user_id"));
		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
			return null;
		}
	}
}
