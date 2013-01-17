package org.whispercomm.manes.exp.gpsmeasurer;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

/**
 * Sensor of GPS location.
 * 
 * @author Yue Liu
 */
public class GpsSensor implements Sensor, LocationListener {

	public static final String LOG_NAME = "GPS-location.dat";
	public static final long EXPIRE_TIME = 3000;
	private Context context;
	private LocationManager locationManager;
	private MyGpsListener gpsListener;
	public long mLastLocationMillis;
	private Location mLastLocation;
	private boolean isGpsFixed;
	private UiHandler uiHandler;
	//private FileLogger locationLogger;
	EasyWakeLock wakeLock;
	/**
	 * Minimum time interval between location changes (in milliseconds).
	 */
	private long updateInterval;

	public GpsSensor(Context context, UiHandler uiHandler) {
		this.context = context;
		this.locationManager = null;
		this.mLastLocation = null;
		this.isGpsFixed = false;
		this.uiHandler = uiHandler;
		//this.locationLogger = null;
		this.wakeLock = new EasyWakeLock(context);
	}

	@Override
	public void start(int interval) {
		// grab wake lock
		wakeLock.acquire();
		uiHandler.appendToTerminal("GPS measuring started.");
		updateInterval = interval * 1000;
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		gpsListener = new MyGpsListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				updateInterval, 0, this);
		locationManager.addGpsStatusListener(gpsListener);
//		try {
//			locationLogger = new FileLogger(LOG_NAME);
//		} catch (IOException ex) {
//			uiHandler.appendToTerminal("!!!Cannot open log file!!!");
//		}
	}

	@Override
	public void stop() {
		uiHandler.appendToTerminal("GPS measuring stopped.");
//		if (locationLogger != null) {
//			try {
//				locationLogger.close();
//			} catch (IOException ex) {
//				uiHandler.appendToTerminal("!!!Cannot close log file!!!");
//			}
//		}
		if (locationManager != null) {
			locationManager.removeGpsStatusListener(gpsListener);
			locationManager.removeUpdates(this);
			locationManager = null;
		}
		mLastLocation = null;
		isGpsFixed = false;
		// release wake lock
		wakeLock.release();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null) {
			return;
		}
		// log the new location
		String message = String.valueOf(System.currentTimeMillis()) + "\t"
				+ String.valueOf(location.getLatitude()) + "\t"
				+ String.valueOf(location.getLongitude());
		uiHandler.appendToTerminal("New location:\n" + message);
//		try {
//			locationLogger.append(message);
//
//		} catch (IOException e) {
//			uiHandler.appendToTerminal("!!!Failed to log this new location!!!");
//		}
		mLastLocation = location;
		mLastLocationMillis = SystemClock.elapsedRealtime();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Debug
		uiHandler.appendToTerminal("!!!GPS providered disabled!!!");
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private class MyGpsListener implements GpsStatus.Listener {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				if (mLastLocation != null) {
					isGpsFixed = ((SystemClock.elapsedRealtime() - mLastLocationMillis) < EXPIRE_TIME);
				}
				// Currently do nothing when GPS fix gets lost but warn the
				// user.
				if (isGpsFixed == false) {
					//String message = String.valueOf(System.currentTimeMillis());
					uiHandler.appendToTerminal("!!!Lost GPS fix!!!");
//					try {
//						locationLogger.append(message);
//
//					} catch (IOException e) {
//						uiHandler
//								.appendToTerminal("!!!Failed to log this new location!!!");
//					}
				}
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				isGpsFixed = true;
				break;
			}
		}
	}

}