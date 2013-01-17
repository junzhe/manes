package org.whispercomm.manes.exp.locationsensor.location.actuator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.util.PeriodicExecutor;

/**
 * This class provides interfaces for conducting a Wifi scan.
 * <p>
 * Note that because of the way we holds a non-reference-counted
 * {@link WifiLock}, any scan request that is delivered before the previous
 * request's results return may be silently dropped.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 */
public class WifiScanner implements SensorActuator {

	public static final String TAG = WifiScanner.class.getSimpleName();
	/**
	 * The operation mode when Wifi card is waked up via the acquiring of a Wifi
	 * lock.
	 */
	public static final int WIFI_MODE_WAKEUP = WifiManager.WIFI_MODE_SCAN_ONLY;

	private final Context context;
	private WifiManager wifiManager;
	private final WifiLock wifiLock;
	private PeriodicExecutor executor;
	private WifiMeasurer wifiMeasurer;
	private boolean isStarted;
	private boolean isRegistered;

	/**
	 * Initiates an instance of {@link WifiScanner}.
	 * 
	 * @param context
	 *            the context used to get a {@link WifiManager}.
	 */
	public WifiScanner(Context context) {
		this.context = context;
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		this.wifiLock = wifiManager.createWifiLock(WIFI_MODE_WAKEUP, TAG
				+ ".wifilock");
		if (wifiLock != null)
			wifiLock.setReferenceCounted(false);
		this.wifiMeasurer = new WifiMeasurer();
		this.executor = new PeriodicExecutor(context, TAG + ".wifiscan",
				wifiMeasurer);
		this.isStarted = false;
		this.isRegistered = false;
	}

	@Override
	synchronized public boolean startPeriodicMeasures(long period) {
		if (isStarted)
			return false;
		executor.start(period, true);
		isStarted = true;
		return true;

	}

	@Override
	synchronized public boolean stopPeriodicMeasures() {
		if (isStarted == false)
			return true;
		executor.stop();
		isStarted = false;
		return true;
	}

	/**
	 * Start a Wifi measurement.
	 * <p>
	 * Confirmed that we'll always get a scan result back no matter whether
	 * there is Wifi signal. If there is no Wifi signal, we'll get an empty list
	 * of APs back. Therefore, we do not have the danger of holding wifi lock
	 * forever when there is no Wifi signal.
	 * <p>
	 * It is important to note that one Wifi measure does not necessarily
	 * results in a scan-result-available event. Because wifi scan takes time,
	 * it is possible that multiple scans result in a single
	 * scan-result-available event.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public class WifiMeasurer extends BroadcastReceiver implements Runnable {

		synchronized public void measure() {
			Log.i(System.currentTimeMillis() + "\t" + TAG, "Start a wifi scan!");
			wifiLock.acquire();
			if (isRegistered == false) {
				context.registerReceiver(this, new IntentFilter(
						WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				isRegistered = true;
			}
			wifiManager.startScan();
		}

		@Override
		public void run() {
			measure();
		}

		@Override
		synchronized public void onReceive(Context arg0, Intent arg1) {
			Log.i(System.currentTimeMillis() + "\t" + TAG,
					"Wifi scan results available. Unregister wifiscanner for scan results!");
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			isRegistered = false;
			wifiLock.release();
		}
	}

	@Override
	public void setOperator(SensorOperator operator) {
		// do nothing here.
	}

	@Override
	public void shutDown() {
		executor.stop();
		isStarted = false;
	}
}
