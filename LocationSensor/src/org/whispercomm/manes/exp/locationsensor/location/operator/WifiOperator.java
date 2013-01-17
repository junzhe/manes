package org.whispercomm.manes.exp.locationsensor.location.operator;

import org.whispercomm.manes.exp.locationsensor.data.Wifis;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.exp.locationsensor.location.sensor.WifiSensor;

/**
 * This class is in charge of scheduling Wifi scans.
 * 
 * @author Yue Liu
 * 
 */
public class WifiOperator implements SensorOperator {

	public static final String TAG = WifiOperator.class.getSimpleName();

	public static final long SCAN_INTERVAL_STATIC = 2 * 60 * 1000;
	public static final long SCAN_INTERVAL_FAIL_SAFE = 2 * SCAN_INTERVAL_STATIC;
	public static final long SCAN_INTERVAL_MOBILE = 30 * 1000;

	private Wifis wifiCrt;
	private Wifis wifiAtLastGps;
	private final WifiSensor wifiSensor;

	public WifiOperator(WifiSensor wifiSensor) {
		this.wifiCrt = null;
		this.wifiAtLastGps = null;
		this.wifiSensor = wifiSensor;
	}

	@Override
	synchronized public void inform(SensorSignal signal) {
		// do nothing here
		if (signal == SensorSignal.WIFI_AVAILABLE) {
			// update Wifi readings history
			wifiCrt = wifiSensor.getLatestReading();
		}
		if (signal == SensorSignal.GPS_AVAILABLE) {
			wifiAtLastGps = wifiCrt;
		}
	}

	/**
	 * Decide whether Wifi measurement has changed significantly enough since
	 * last GPS measurement to justify a new GPS measurement.
	 * 
	 * @return
	 */
	public boolean hasWifiChangedSinceLastGps() {
		if (wifiCrt == null || wifiAtLastGps == null) {
			// not enough information to make the decision. Assume false.
			return false;
		}
		return WifiOperatorPolicy.hasWifiEnvironmentChanged(wifiAtLastGps,
				wifiCrt);
	}

	/**
	 * Whether we got Wifi signal at least once at last GPS measurement and
	 * current.
	 * 
	 * @return
	 */
	public boolean gotWifiSignalSinceLastGps() {
		if (wifiCrt == null || wifiAtLastGps == null)
			return false;
		if (wifiCrt.getWifi() == null || wifiAtLastGps.getWifi() == null)
			return false;
		if (wifiCrt.getWifi().size() == 0
				&& wifiAtLastGps.getWifi().size() == 0)
			return false;
		return true;
	}

}
