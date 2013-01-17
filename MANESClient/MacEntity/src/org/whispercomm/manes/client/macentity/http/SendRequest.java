package org.whispercomm.manes.client.macentity.http;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispercomm.manes.client.macentity.network.ManesService;

import android.util.Base64;
import android.util.Log;

/**
 * Wrapper class for send requests to the MANES server
 * 
 * @author David Adrian
 * 
 */
public final class SendRequest extends HttpPost {
	private static final String TAG = SendRequest.class.getSimpleName();

	private static final String KEY_APP_ID = "app_id";
	private static final String KEY_CONTENTS = "contents";

	private int appId;
	private String encodedContents;

	public SendRequest(long userId, int appId, byte[] contents) {
		super(ManesService.SERVER_URL + "/user/" + userId + "/packet/");
		this.appId = appId;
		this.encodedContents = new String(Base64.encode(contents,
				Base64.DEFAULT));
		this.prepareRequest();
	}

	private void prepareRequest() {
		try {
			// Create the JSON
			JSONObject data = new JSONObject();
			data.put(KEY_APP_ID, this.appId);
			data.put(KEY_CONTENTS, this.encodedContents);

			// Convert the JSON to an HttpEntity and hand it to the request
			this.setHeader("Content-Type", "application/json");
			this.setEntity(new StringEntity(data.toString()));
		} catch (JSONException e) {
			// Should happen only due to buggy code, and thus should be caught
			// by tests.
			Log.e(TAG, "failed to prepare SendRequest.", e);
		} catch (UnsupportedEncodingException e) {
			// Should happen only due to buggy code, and thus should be caught
			// by tests.
			Log.e(TAG, "failed to prepare SendRequest.", e);
		}
	}
}
