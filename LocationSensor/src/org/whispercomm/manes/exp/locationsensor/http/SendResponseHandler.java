package org.whispercomm.manes.exp.locationsensor.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import android.util.Log;

/**
 * {@link ResponseHandler} for {@link SendRequest}
 * 
 * @author David Adrian
 * 
 */
public final class SendResponseHandler implements ResponseHandler<Boolean> {
	private static final String TAG = SendResponseHandler.class.getSimpleName();

	private static final int STATUS_CODE_SUCCESS_SEND = 201;

	@Override
	public Boolean handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode != STATUS_CODE_SUCCESS_SEND) {
			Log.w(TAG, String.format(
					"Send request failed.\nStatus code: %s\nReason phrase: %s",
					statusCode, statusLine.getReasonPhrase()));
			return false;
		}
		return true;
	}
}
