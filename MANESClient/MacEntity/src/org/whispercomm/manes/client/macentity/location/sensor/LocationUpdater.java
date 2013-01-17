package org.whispercomm.manes.client.macentity.location.sensor;

import java.util.concurrent.locks.ReentrantLock;

import org.whispercomm.manes.client.macentity.location.LocationSender;
import org.whispercomm.manes.client.macentity.location.ServerUnSyncedException;
import org.whispercomm.manes.client.macentity.location.TopologyServerSynchronizer;
import org.whispercomm.manes.client.macentity.location.actuator.LocationUpdateActuator;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.operator.GpsOperator;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.location.operator.WifiOperator;
import org.whispercomm.manes.client.macentity.location.operator.WifiOperatorPolicy;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifis;

import android.content.Context;

/**
 * This class implements both {@link LocationSensor} and {@link SensorOpeartor}
 * interfaces. It is in charge of sending location updates to the topology
 * server.
 * 
 * @author Yue Liu
 * 
 */
@SuppressWarnings("rawtypes")
public class LocationUpdater implements SensorOperator, LocationSensor {

	public static final String TAG = LocationUpdater.class.getSimpleName();

	/**
	 * Location update period.
	 */
	private static final long UPDATE_PERIOD = 5 * 60 * 1000;
	private static final long WIFI_DELAY_TOLERABLE = WifiOperator.SCAN_INTERVAL_MOBILE / 2;
	private static final long GPS_DELAY_TOLERABLE = GpsOperator.GPS_MEASURE_INTERVAL_MIN / 2;

	private LocationUpdateActuator locationUpdateActuator;
	private final TopologyServerSynchronizer synchronizer;
	private final GpsSensor gpsSensor;
	private final WifiSensor wifiSensor;
	private Context context;
	private LocationSender locationSender;
	private GeneralOperator operator;
	private boolean isWorking;
	private ReentrantLock isWorkingLock;

	public LocationUpdater(Context context, LocationSender locationSender,
			TopologyServerSynchronizer synchronizer, GpsSensor gpsSensor,
			WifiSensor wifiSensor) {
		this.synchronizer = synchronizer;
		this.gpsSensor = gpsSensor;
		this.wifiSensor = wifiSensor;
		this.operator = null;
		this.locationUpdateActuator = null;
		this.locationSender = locationSender;
		this.context = context;
		this.isWorking = false;
		this.isWorkingLock = new ReentrantLock();
	}

	@Override
	public void inform(SensorSignal signal) {
		long crtTime = System.currentTimeMillis();
		if (signal == SensorSignal.LOC_UPDATE_ACTUATION) {
			startOneMeasureBy(crtTime + UPDATE_PERIOD);
		}
		if (signal == SensorSignal.GPS_AVAILABLE) {
			if (isWorthUpdating()) {
//				Log.i(TAG, "Sensor readings worth updating!");
				startOneMeasureBy(crtTime + GPS_DELAY_TOLERABLE);
			}
		}
		if (signal == SensorSignal.WIFI_AVAILABLE) {
			if (isWorthUpdating()) {
//				Log.i(TAG, "Sensor readings worth updating!");
				startOneMeasureBy(crtTime + WIFI_DELAY_TOLERABLE);
			}
		}
	}

	/**
	 * Decide whether the new sensor readings are different enough from what the
	 * topology server already has to initiate a new location update.
	 * 
	 * @return
	 */
	private boolean isWorthUpdating() {
		Location locationServer;
		try {
			locationServer = synchronizer.getLatestServerRecord();
		} catch (ServerUnSyncedException e) {
			return true;
		}
		Wifis wifiServer = locationServer.getWifi();
		Wifis wifiCrt = wifiSensor.getLatestReading();
		if (wifiServer != null && wifiCrt != null) {
			if (WifiOperatorPolicy
					.hasSignificantWifiChange(wifiServer, wifiCrt))
				return true;
		}
		if ((wifiServer == null && wifiCrt != null)
				|| (wifiServer != null && wifiCrt == null))
			return true;
		GPS gpsServer = locationServer.getGps();
		GPS gpsCrt = gpsSensor.getLatestReading();
		if (gpsServer != null && gpsCrt != null) {
			if (GPS.getDistance(gpsServer, gpsCrt) > GpsOperator.GPS_ACCURACY)
				return true;
		}
		if ((gpsServer == null && gpsCrt != null)
				|| (gpsServer != null && gpsCrt == null))
			return true;
		return false;
	}

	@Override
	public void startSensing() {
		isWorkingLock.lock();
		try {
			if (isWorking == false && locationUpdateActuator == null) {
				isWorking = true;
				locationUpdateActuator = new LocationUpdateActuator(context,
						locationSender, operator);
				locationUpdateActuator.startOneMeasureAt(System
						.currentTimeMillis() + GPS_DELAY_TOLERABLE);
			}
		} finally {
			isWorkingLock.unlock();
		}
	}

	@Override
	public void stopSensing() {
		isWorkingLock.lock();
		if (isWorking) {
			isWorking = false;
			isWorkingLock.unlock();
			locationUpdateActuator.shutDown();
			locationUpdateActuator = null;
		} else {
			isWorkingLock.unlock();
		}
	}

	@Override
	public void updateReadings(Object newReadings) {
		// do nothing here

	}

	@Override
	public Object getLatestReading() {
		// do nothing here
		return null;
	}

	@Override
	public boolean isSensing() {
		// do nothing here
		return false;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = (GeneralOperator) operator;
		if (locationUpdateActuator != null)
			locationUpdateActuator.setOperator(operator);
	}

	@Override
	public void startOneMeasureBy(long execTime) {
		isWorkingLock.lock();
		try {
			if (isWorking)
				locationUpdateActuator.startOneMeasureBy(execTime);
		} finally {
			isWorkingLock.unlock();
		}
	}

	@Override
	public void startOneMeasureAt(long execTime) {
		isWorkingLock.lock();
		try {
			if (isWorking)
				locationUpdateActuator.startOneMeasureAt(execTime);
		} finally {
			isWorkingLock.unlock();
		}
	}
}
