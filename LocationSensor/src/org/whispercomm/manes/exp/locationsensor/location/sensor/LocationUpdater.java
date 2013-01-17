package org.whispercomm.manes.exp.locationsensor.location.sensor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.whispercomm.manes.exp.locationsensor.http.HttpManager;
import org.whispercomm.manes.exp.locationsensor.http.ManesHttpException;
import org.whispercomm.manes.exp.locationsensor.http.MotionPositionDataRequest;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.network.IdManager;
import org.whispercomm.manes.exp.locationsensor.network.NotRegisteredException;
import org.whispercomm.manes.exp.locationsensor.util.PeriodicExecutor;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class LocationUpdater<T> implements LocationSensor<T> {

	public static final String TAG = LocationUpdater.class.getSimpleName();
	public static final long UPDATE_PERIOD = 30 * 60 * 1000;
	@SuppressLint("SdCardPath")
	private static final String TMP_DATA_DIR = "/sdcard/";

	private final HttpManager httpManager;
	private final IdManager idManager;
	private ConnectivityManager connManager;
	private PeriodicExecutor executor;
	/**
	 * Stores all data from the sensor after last update.
	 */
	private List<T> currentData;
	/**
	 * Stores all data that has been attempted to upload but was not successful
	 */
	private List<T> prevData;
	private boolean isPostingLocation;
	private long userId;
	private String url;
	private PostLocation postLocation;
	private PostLocationResponseHandler responseHandler;
	private boolean isSensing;
	private FileWriter tmpDataRecord;
	private ObjectMapper objectMapper;
	private Class<T> dataClass;

	public LocationUpdater(Context context, HttpManager httpManager,
			IdManager idManager, ConnectivityManager connManager, String url,
			Class<T> dataClass) {
		this.httpManager = httpManager;
		this.idManager = idManager;
		this.currentData = new LinkedList<T>();
		this.prevData = new LinkedList<T>();
		this.isPostingLocation = false;
		this.url = url;
		this.postLocation = new PostLocation();
		this.responseHandler = new PostLocationResponseHandler();
		this.executor = new PeriodicExecutor(context, TAG + "." + url,
				postLocation);
		this.isSensing = false;
		this.connManager = connManager;
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.dataClass = dataClass;
	}

	@Override
	public void startSensing() {
		executor.start(UPDATE_PERIOD, true);
		isSensing = true;
	}

	@Override
	synchronized public void stopSensing() {
		executor.stop();
		isSensing = false;
		isPostingLocation = false;
		if (currentData.size() != 0 || prevData.size() != 0) {
			try {
				backupDataToFile();
			} catch (IOException e) {
				Log.e(TAG + "." + url, e.getMessage(), e);
			}
		}
	}

	@Override
	public void startPeriodicMeasures(long peirod) {
		// do nothing here.

	}

	@Override
	synchronized public void updateReadings(T newReadings) {
		// Log.i(TAG + "." + url, "Add new sensor readngs to buffer");
		currentData.add(newReadings);
	}

	@Override
	public T getLatestReading() {
		// do nothing here
		return null;
	}

	@Override
	public boolean isSensing() {
		return isSensing;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		// do nothing here
	}

	private void backupDataToFile() throws IOException {
		// cannot send the data because there is no Wifi connection now
		prevData.addAll(currentData);
		// append prevData into a tmp. storage file
		Iterator<T> it = prevData.iterator();
		while (it.hasNext()) {
			tmpDataRecord = new FileWriter(TMP_DATA_DIR + url + "-tmp.json",
					true);
			objectMapper.writeValue(tmpDataRecord, it.next());
		}
		// clear up prevData
		prevData.clear();
		currentData.clear();
	}

	private void importDataFromFile() throws JsonProcessingException,
			IOException {
		File tmpFile = new File(TMP_DATA_DIR + url + "-tmp.json");
		if (tmpFile.exists()) {
			// makes sure the content from backup file is injected into
			// prevData first
			Iterator<T> itt = objectMapper.reader(dataClass)
					.readValues(tmpFile);
			while (itt.hasNext()) {
				T next = itt.next();
				// Log.i(TAG + "." + url,
				// "***data-from-file: " + next.toString());
				prevData.add(next);
			}
			// remove the backup file
			tmpFile.delete();
		}
	}

	synchronized private void postLocation() {
		// Let's print out prevData
		Log.i(TAG + "." + url, "*********attempting to post location update.");
		if (isPostingLocation) {
			// do not try to post location again if we are already in the
			// process of posting location.
			Log.i(TAG + "." + url, "already in the posting process. Return.");
			return;
		}
		try {
			isPostingLocation = true;
			// check whether we are connected with Internet via WiFi
			userId = idManager.getUserId();
			if (hasWifiConnection() == false) {
				Log.i(TAG + "." + url, "No Wifi connection.");
				backupDataToFile();
				return;
			}
			Log.i(TAG + "." + url, "Have Wifi connection. Send data now.");
			importDataFromFile();
			prevData.addAll(currentData);
			currentData.clear();
			if (prevData.size() == 0) {
				Log.i(TAG + "." + url,
						"No location data to upload now. Return.");
				return;
			} else {
				Log.i(TAG + "." + url, "number of data: " + prevData.size());
			}
			MotionPositionDataRequest<T> locationPost = new MotionPositionDataRequest<T>(
					url, userId, prevData, objectMapper);
			httpManager.submit(locationPost, responseHandler);
		} catch (ManesHttpException e) {
			Log.e(TAG + "." + url, e.getMessage(), e);
		} catch (NotRegisteredException e) {
			Log.e(TAG + "." + url, e.getMessage(), e);
		} catch (JsonGenerationException e) {
			Log.e(TAG + "." + url, e.getMessage(), e);
		} catch (JsonMappingException e) {
			Log.e(TAG + "." + url, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG + "." + url, e.getMessage(), e);
		} finally {
			isPostingLocation = false;
		}
	}

	private boolean hasWifiConnection() {
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo == null)
			return false;
		if (networkInfo.isConnected() == false)
			return false;
		int type = networkInfo.getType();
		Log.i(TAG + "." + url, String.valueOf(type));
		if (type == ConnectivityManager.TYPE_WIFI)
			return true;
		else
			return false;
	}

	synchronized private void handlePostLocationResponse(int statusCode) {
		if (statusCode == 201) {
			Log.i(TAG + "." + url, "Location update succeeds!");
			prevData.clear();
		} else {
			Log.i(TAG + "." + url,
					"Location update fails. Keep un-uploaded data for next time.");
		}
		isPostingLocation = false;
	}

	/**
	 * Wrapper {@link Runnable} for posting location update to the server.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class PostLocation implements Runnable {

		@Override
		public void run() {
			postLocation();
		}

	}

	/**
	 * Handle http response from posting location update.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class PostLocationResponseHandler implements
			ResponseHandler<Integer> {

		@Override
		public Integer handleResponse(HttpResponse response) {
			int statusCode = response.getStatusLine().getStatusCode();
			handlePostLocationResponse(statusCode);
			return statusCode;
		}

	}
}
