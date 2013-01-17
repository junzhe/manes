package org.whispercomm.manes.client.macentity.location.operator;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;
import org.whispercomm.manes.client.macentity.util.BoundedFifo;
import org.whispercomm.manes.topology.location.Wifis;

/**
 * This class is in charge of scheduling Wifi scans.
 * 
 * @author Yue Liu
 * 
 */
public class WifiOperator implements SensorOperator {

	public static final String TAG = WifiOperator.class.getSimpleName();

	/**
	 * The number of historical sensor readings stored in this operator.
	 */
	private static final int SENSOR_HISTORY_SIZE = 2;
	public static final long SCAN_INTERVAL_STATIC = 2 * 60 * 1000;
	public static final long SCAN_INTERVAL_FAIL_SAFE = 2 * SCAN_INTERVAL_STATIC;
	public static final long SCAN_INTERVAL_MOBILE = 30 * 1000;

	private GeneralOperator locationOperator;
	private WifiSensor wifiSensor;
	/**
	 * Historical sensor actuation times.
	 */
	private BoundedFifo<Long> sensorTimes;
	/**
	 * Historical sensor readings.
	 * <p>
	 * Note that the length of {@code sensorTimes} is not necessarily the same
	 * as that of {@code SensorReadings}, because multiple scan requests may
	 * result in a single scan-result-available event.
	 */
	private BoundedFifo<Wifis> sensorReadings;
	private Wifis wifiCrt;
	/**
	 * The Wifi measurement result at last time GPS measurement results were
	 * available.
	 */
	private Wifis wifiAtLastGps;

	public WifiOperator(WifiSensor wifiSensor, GeneralOperator locationOperator) {
		this.locationOperator = locationOperator;
		this.wifiSensor = wifiSensor;
		this.sensorTimes = new BoundedFifo<Long>(SENSOR_HISTORY_SIZE);
		this.sensorReadings = new BoundedFifo<Wifis>(SENSOR_HISTORY_SIZE);
		this.wifiCrt = null;
		this.wifiAtLastGps = null;
	}

	@Override
	synchronized public void inform(SensorSignal signal) {
		long crtTime = System.currentTimeMillis();
		if (signal == SensorSignal.WIFI_ACTUATION) {
			sensorTimes.add(crtTime);
			// Schedule a measure here in case we do not get scan result back
			// this time, we'll still be able to resume scanning after
			// SCAN_INTERVAL_FAIL_SAFE.
			wifiSensor.startOneMeasureBy(crtTime + SCAN_INTERVAL_FAIL_SAFE);
		}
		if (signal == SensorSignal.WIFI_AVAILABLE) {
			// update Wifi readings history
			Wifis wifiPrev = wifiCrt;
			wifiCrt = wifiSensor.getLatestReading();
			sensorReadings.add(wifiCrt);
			// schedule next wifi scan.
			scheduleNextMeasure();
			// Decide whether the Wifi signal turned from strong to weak, and
			// signal GPS operator, if necessary.
			signalWifiTurnedWeak(wifiPrev, wifiCrt);
		}
		if (signal == SensorSignal.GPS_AVAILABLE) {
			wifiAtLastGps = wifiCrt;
		}
	}

	/**
	 * Decide whether the Wifi signal turned from strong to weak, and signal GPS
	 * operator, if necessary.
	 * 
	 * @param wifiPrev
	 * @param wifiCrt
	 */
	private void signalWifiTurnedWeak(Wifis wifiPrev, Wifis wifiCrt) {
		if (wifiPrev != null && wifiCrt != null) {
			if (WifiOperatorPolicy.isWifiStrong(wifiPrev) == true
					&& WifiOperatorPolicy.isWifiStrong(wifiCrt) == false) {
				locationOperator.inform(SensorSignal.WIFI_TURNED_WEAK);
			}
		}
	}

	/**
	 * Schedule next Wifi according to previous two measure results.
	 */
	private void scheduleNextMeasure() {
		long crtTime = System.currentTimeMillis();
		int size = sensorReadings.size();
		if (size < 2) {
			// not enough history to make decision. Schedule next sensing with
			// largest delay if there is no earlier ones.
			wifiSensor.startOneMeasureAt(crtTime + SCAN_INTERVAL_STATIC);
			return;
		}

		Wifis wifiCrt = sensorReadings.get(size - 1);
		Wifis wifiPrev = sensorReadings.get(size - 2);
		if (wifiPrev != null && wifiCrt != null) {
			if (WifiOperatorPolicy.hasSignificantWifiChange(
					sensorReadings.get(size - 1), sensorReadings.get(size - 2))) {
				wifiSensor.startOneMeasureAt(crtTime + SCAN_INTERVAL_MOBILE);
			} else
				wifiSensor.startOneMeasureAt(crtTime + SCAN_INTERVAL_STATIC);
		} else if ((wifiPrev == null && wifiCrt != null)
				|| (wifiPrev != null && wifiCrt == null)) {
			wifiSensor.startOneMeasureAt(crtTime + SCAN_INTERVAL_MOBILE);
		} else {
			wifiSensor.startOneMeasureAt(crtTime + SCAN_INTERVAL_STATIC);
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
