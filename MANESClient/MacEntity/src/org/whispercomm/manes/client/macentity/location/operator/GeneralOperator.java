package org.whispercomm.manes.client.macentity.location.operator;

import org.whispercomm.manes.client.macentity.location.ManesLocationManager;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.location.sensor.LocationUpdater;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;

import android.content.Context;

/**
 * This {@link SensorOperator} is a central operator that directly takes in all
 * signals from various {@link LocationSensor}s and de-multiplex them to
 * specific operators.
 * 
 * @author Yue Liu
 * 
 */
public class GeneralOperator implements SensorOperator {

	/**
	 * This "enum" defines various signals that can be handled by various
	 * {@link SensorOperators}.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public enum SensorSignal {
		/**
		 * Notice for GPS measure results available.
		 */
		GPS_AVAILABLE,
		/**
		 * Fake notice for GPS measure results available. It is issued when we
		 * decide to skip a GPS measurement when Wifi have not changed since
		 * last GPS measurement.
		 */
		FAKE_GPS_AVAILABLE,
		/**
		 * Notice for having just actuated a GPS measure.
		 */
		GPS_ACTUATION,
		/**
		 * Notice for Wifi scan results available.
		 */
		WIFI_AVAILABLE,
		/**
		 * Notice for having just actuated a Wifi scan.
		 */
		WIFI_ACTUATION,
		/**
		 * Notice for Wifi signals have turned from strong to weak.
		 */
		WIFI_TURNED_WEAK,
		/**
		 * Notice for having sent a location update.
		 */
		LOC_UPDATE_ACTUATION
	}

	public static final String TAG = GeneralOperator.class.getSimpleName();

	private final LocationUpdater locationUpdater;
	private GpsOperator gpsOperator;
	private WifiOperator wifiOperator;
	private boolean enforceGpsMeasure;

	public GeneralOperator(Context context,
			ManesLocationManager manesLocManager,
			LocationUpdater locationUpdator, GpsSensor gpsSensor,
			WifiSensor wifiSensor) {
		this.locationUpdater = locationUpdator;
		this.gpsOperator = new GpsOperator(gpsSensor);
		this.wifiOperator = new WifiOperator(wifiSensor, this);
		this.enforceGpsMeasure = false;
	}

	@Override
	// TODO perhaps???? have informwhateversignal() method...
	public void inform(SensorSignal signal) {
		if (signal == SensorSignal.WIFI_TURNED_WEAK) {
			enforceGpsMeasure = true;
		}
		locationUpdater.inform(signal);
		gpsOperator.inform(signal);
		wifiOperator.inform(signal);
	}

	/**
	 * This will decide not to initiate a GPS measurement if the last one did
	 * not catch valid signal and the Wifi environment has not shown significant
	 * change since that. This also carries the indication that the GPS
	 * measurement is the same as last time.
	 * 
	 * @return
	 */
	public boolean shouldNotInitiateNewGpsMeasure() {
		if (enforceGpsMeasure) {
			enforceGpsMeasure = false;
			return false;
		}
		if (gpsOperator.gotGpsSignalLastTime() == false
				&& wifiOperator.gotWifiSignalSinceLastGps() == true
				&& wifiOperator.hasWifiChangedSinceLastGps() == false)
			return true;
		else
			return false;
	}
}
