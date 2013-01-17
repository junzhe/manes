package org.whispercomm.manes.exp.locationsensor.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import org.whispercomm.manes.exp.locationsensor.http.HttpManager;
import org.whispercomm.manes.exp.locationsensor.http.ManesHttpException;
import org.whispercomm.manes.exp.locationsensor.location.ManesLocationManager;

/**
 * This is a background service that keeps logging location sensor data.
 * 
 * @author Yue Liu
 */
public class ManesService extends Service {

	private static final String TAG = ManesService.class.getName();
	public static final String SERVER_ADDRESS = "54.243.140.112";
	public static final String SERVER_URL = "http://" + SERVER_ADDRESS
			+ ":8890";
	public static final String APP_ID_INTENT_KEY = "app_id";
	private HttpManager httpManager;
	private IdManager idManager;
	private ManesLocationManager locationManager;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate() called.  Starting ManesService.");
		this.httpManager = new HttpManager();
		this.idManager = new IdManager(this.getApplicationContext());
		// Register to the server.
		try {
			idManager.getUserId();
		} catch (NotRegisteredException e1) {
			// Only register if the user id does not exist
			Log.i(TAG, "Not registered before. Register to the server now.");
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						idManager.register(httpManager);
					} catch (ManesHttpException e) {
						Log.e(TAG, e.getMessage(), e);
						return;
					}
				}

			}).start();
		}
		this.locationManager = new ManesLocationManager(Looper.getMainLooper(),
				httpManager, idManager, this);
		locationManager.start();
		Log.i(TAG, "Started.");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy() called.  Stopping ManesService.");
		if (locationManager != null) {
			locationManager.stop();
		}
		httpManager.shutdown();
		Log.i(TAG, "Stopped.");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
}
