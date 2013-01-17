package org.whispercomm.manes.client.macentity.location.actuator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.util.TimedExecutor;

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
public class WifiScanner extends AbstractActuator {

	public static final String TAG = WifiScanner.class.getSimpleName();
	/**
	 * The operation mode when Wifi card is waked up via the acquiring of a Wifi
	 * lock.
	 */
	public static final int WIFI_MODE_WAKEUP = WifiManager.WIFI_MODE_SCAN_ONLY;

	private final Context context;
	private SensorOperator operator;
	private WifiManager wifiManager;
	private final WifiLock wifiLock;
	private TimedExecutor executor;
	private WifiMeasurer wifiMeasurer;

	/**
	 * Initiates an instance of {@link WifiScanner}.
	 * 
	 * @param context
	 *            the context used to get a {@link WifiManager}.
	 * @param operator
	 *            the operator to be informed when meaningful events happen.
	 */
	public WifiScanner(Context context, SensorOperator operator) {
		this.context = context;
		this.operator = operator;
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		this.wifiLock = wifiManager.createWifiLock(WIFI_MODE_WAKEUP, TAG
				+ ".wifilock");
		if (wifiLock != null)
			wifiLock.setReferenceCounted(false);
		this.executor = new TimedExecutor(context);
		this.wifiMeasurer = new WifiMeasurer();
	}

	/**
	 * This methods allows the caller to start one Wifi scan at the specified
	 * time.
	 * 
	 */
	@Override
	public void startOneMeasureAt(long execTime) {
		cancelPendingMeasures();
		executor.schedule(execTime, wifiMeasurer);
//		Log.i(System.currentTimeMillis() + "\t" + TAG,
//				"next sensor measure is scheduled "
//						+ (execTime - System.currentTimeMillis()) + " later.");
	}

	@Override
	protected Long getNextActuationTime() {
		return executor.getNextExecTime();
	}

	@Override
	protected void cancelPendingMeasures() {
		executor.cancelAllPendingJobs();
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

		public void measure() {
			Log.i(System.currentTimeMillis() + "\t" + TAG, "Start a wifi scan.");
			wifiLock.acquire();
			context.registerReceiver(this, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			wifiManager.startScan();
			// update WifiOperator
			if (operator != null) {
				operator.inform(SensorSignal.WIFI_ACTUATION);
			}
		}

		@Override
		public void run() {
			measure();
		}

		@Override
		public void onReceive(Context arg0, Intent arg1) {
//			Log.i(System.currentTimeMillis() + "\t" + TAG,
//					"Wifi scan results available. Unregister wifiscanner for scan results!");
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			wifiLock.release();
		}
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = operator;
	}

	@Override
	public void shutDown() {
		executor.shutDown();
	}
}
