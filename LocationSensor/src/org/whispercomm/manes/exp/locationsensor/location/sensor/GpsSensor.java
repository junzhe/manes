package org.whispercomm.manes.exp.locationsensor.location.sensor;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

import org.whispercomm.manes.exp.locationsensor.data.GPS;
import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.location.actuator.GpsActuator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

/**
 * GPS sensor.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
public class GpsSensor implements LocationSensor<GPS>, LocationListener {

	public static final String TAG = GpsSensor.class.getSimpleName();
	public static final long EXPIRE_TIME = 20000;
	public static final long GPS_MEASURE_PERIOD_STATIC = 60000;

	private LocationManager locationManager;
	private GpsStatusListener gpsListener;
	private GpsActuator gpsActuator;
	private GeneralOperator operator;
	public long mLastLocationMillis;
	private boolean isWorking;
	private boolean isFixed;
	private GPS gpsReading;
	private Context context;
	private ReentrantLock isWorkingLock;

	public GpsSensor(Context context) {
		this.context = context;
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		this.gpsListener = new GpsStatusListener();
		this.mLastLocationMillis = 0;
		this.isWorking = false;
		this.isFixed = false;
		this.gpsReading = null;
		this.operator = null;
		this.gpsActuator = null;
		this.isWorkingLock = new ReentrantLock();
	}

	public void startSensing() {
		isWorkingLock.lock();
		try {
			if (isWorking == false && gpsActuator == null) {
				isWorking = true;
				gpsActuator = new GpsActuator(context, this, operator);
				locationManager.requestLocationUpdates(
						LocationManager.PASSIVE_PROVIDER, 0, 0, this);
				locationManager.addGpsStatusListener(gpsListener);
				startPeriodicMeasures(GPS_MEASURE_PERIOD_STATIC);
			}
		} finally {
			isWorkingLock.unlock();
		}
	}

	public void stopSensing() {
		isWorkingLock.lock();
		if (isWorking) {
			isWorking = false;
			isWorkingLock.unlock();
			gpsActuator.shutDown();
			gpsActuator = null;
			locationManager.removeGpsStatusListener(gpsListener);
			locationManager.removeUpdates(this);
			isFixed = false;
			gpsReading = null;
		} else {
			isWorkingLock.unlock();
		}
	}

	public GPS getLatestReading() {
		if (isWorking && isFixed) {
			return gpsReading;
		} else {
			return null;
		}
	}

	public boolean isSensing() {
		return isWorking;
	}

	/**
	 * Whether this sensor is picking up valid satellite signal.
	 * 
	 * @return
	 */
	public boolean isFixed() {
		return isFixed;
	}

	/**
	 * This method allows {@link GpsActuator} to set {@code isFixed}.
	 * 
	 * @param isFixed
	 */
	public void setIsFixed(boolean isFixed) {
		this.isFixed = isFixed;
	}

	/**
	 * This method allows {@link GpsActuator} to update {@code gpsReading}.
	 * 
	 * @param location
	 */
	@Override
	synchronized public void updateReadings(GPS location) {
		mLastLocationMillis = SystemClock.elapsedRealtime();
		gpsReading = location;
		// inform operator
		if (operator != null)
			operator.inform(SensorSignal.GPS_AVAILABLE);
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = (GeneralOperator) operator;
		if (gpsActuator != null)
			this.gpsActuator.setOperator(operator);
	}

	private class GpsStatusListener implements GpsStatus.Listener {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				if (gpsReading != null) {
					isFixed = ((SystemClock.elapsedRealtime() - mLastLocationMillis) < EXPIRE_TIME);
				}
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				isFixed = true;
				break;
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;
		if (location.getProvider().compareTo(LocationManager.GPS_PROVIDER) != 0)
			return;
		// Log.i(TAG, "***Update GPS operator with new location!");
		updateReadings(new GPS(HumanReadableTime.getCurrentTime(),
				location.getLatitude(), location.getLongitude()));
	}

	@Override
	public void onProviderDisabled(String arg0) {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
			isFixed = false;
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void startPeriodicMeasures(long period) {
		isWorkingLock.lock();
		try {
			if (isWorking) {
				if (gpsActuator.startPeriodicMeasures(period) == false) {
					gpsActuator.stopPeriodicMeasures();
					gpsActuator.startPeriodicMeasures(period);
				}
			}
		} finally {
			isWorkingLock.unlock();
		}

	}
}
