package org.whispercomm.manes.exp.locationsensor.location.sensor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.Meas;
import org.whispercomm.manes.exp.locationsensor.data.Wifi;
import org.whispercomm.manes.exp.locationsensor.data.Wifis;
import org.whispercomm.manes.exp.locationsensor.location.actuator.WifiScanner;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

/**
 * The Wifi Sensor that measures nearby Wifi networks and their signal strength.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 */
public class WifiSensor extends BroadcastReceiver implements
		LocationSensor<Wifis> {

	public static final String TAG = WifiSensor.class.getSimpleName();
	public static final long WIFI_SCAN_PERIOD_STATIC = 20000;

	/**
	 * Keep the latest sensing results.
	 */
	private Wifis wifiReading;
	/**
	 * The time last Wifi scan results became available.
	 */
	private WifiManager wifiManager;
	private WifiScanner wifiScanner;
	private SensorOperator operator;
	private boolean isWorking;
	private Context context;
	private ReentrantLock isWorkingLock;

	public WifiSensor(Context context) {
		this.context = context;
		this.isWorking = false;
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		this.wifiScanner = null;
		this.wifiReading = null;
		this.operator = null;
		this.isWorkingLock = new ReentrantLock();
	}

	public void startSensing() {
		isWorkingLock.lock();
		try {
			if (isWorking == false && wifiScanner == null) {
				isWorking = true;
				wifiScanner = new WifiScanner(context);
				context.registerReceiver(this, new IntentFilter(
						WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				startPeriodicMeasures(WIFI_SCAN_PERIOD_STATIC);
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
			wifiScanner.shutDown();
			wifiScanner = null;
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.i(TAG, e.getMessage(), e);
			}
		} else {
			isWorkingLock.unlock();
		}
	}

	public Wifis getLatestReading() {
		if (isWorking) {
			return wifiReading;
		} else {
			return null;
		}
	}

	@Override
	public void updateReadings(Wifis reading) {
		wifiReading = reading;
		// update operator
		if (operator != null) {
			operator.inform(SensorSignal.WIFI_AVAILABLE);
		}
	}

	public boolean isSensing() {
		return isWorking;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = operator;
		if (wifiScanner != null)
			this.wifiScanner.setOperator(operator);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.i(System.currentTimeMillis() + "\t" + TAG,
		// "Wifi scan results available!");
		List<ScanResult> wifiResult = wifiManager.getScanResults();
		Wifis wifiReading = translateScanResultToWifis(wifiResult);
		wifiReading.setTime(HumanReadableTime.getCurrentTime());
		// update WifiSensor
		updateReadings(wifiReading);
	}

	public static Wifis translateScanResultToWifis(List<ScanResult> wifiResult) {
		Wifis wifiReading = new Wifis();
		if (wifiResult == null) {
			return wifiReading;
		}
		List<Wifi> apReadings = new LinkedList<Wifi>();
		Wifi apCrt;
		Meas measCrt;
		for (ScanResult result : wifiResult) {
			apCrt = new Wifi();
			measCrt = new Meas();
			measCrt.setFreq(result.frequency);
			measCrt.setRssi(result.level);
			apCrt.setAp(Wifi.TranslateMacToLong(result.BSSID));
			apCrt.setSsid(result.SSID);
			apCrt.setMeas(measCrt);
			apReadings.add(apCrt);
		}
		wifiReading.setWifi(apReadings);
		return wifiReading;
	}

	@Override
	public void startPeriodicMeasures(long period) {
		isWorkingLock.lock();
		try {
			if (isWorking) {
				if (wifiScanner.startPeriodicMeasures(period) == false) {
					wifiScanner.stopPeriodicMeasures();
					wifiScanner.startPeriodicMeasures(period);
				}
			}
		} finally {
			isWorkingLock.unlock();
		}
	}
}
