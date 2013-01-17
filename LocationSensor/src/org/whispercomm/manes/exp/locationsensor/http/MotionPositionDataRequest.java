package org.whispercomm.manes.exp.locationsensor.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.whispercomm.manes.exp.locationsensor.location.ManesLocationManager;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.util.Log;

public class MotionPositionDataRequest<T> extends HttpPost {

	public static final String TAG = MotionPositionDataRequest.class
			.getSimpleName();
	private List<T> data;
	private ObjectMapper objectMapper;
	private String url;

	public MotionPositionDataRequest(String url, long userId, List<T> data,
			ObjectMapper objectMapper) throws ManesHttpException {
		super(ManesLocationManager.SERVER_URL + "/user/" + userId + "/" + url +"/");
		this.url = url;
		this.data = data;
		this.objectMapper = objectMapper;
		this.prepareRequest();
	}

	private void prepareRequest() throws ManesHttpException {
		try {
			// Convert the JSON object to an HttpEntity and hand it to the
			// request
			this.setHeader("Content-Type", "application/json");
			String dataString = objectMapper.writeValueAsString(data);
			this.setEntity(new StringEntity(dataString));
			//Log.d(TAG + "." + url, dataString);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		} catch (JsonGenerationException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		} catch (JsonMappingException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		} catch (IOException e) {
			Log.e(TAG, e.getClass().getName());
			throw new ManesHttpException();
		}
	}
}
