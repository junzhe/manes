package org.whispercomm.manes.exp.wifimeasurer;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiSensor extends BroadcastReceiver implements Sensor {

	public static final String TAG = "org.whispercomm.manes.exp.wifimeasurer.wifisensor";
	public static final String LOG_NAME = "Wifi-location.dat";
	private Context context;
	private WifiManager wifiManager;
	private PeriodicExecutor timer;
	private UiHandler uiHandler;
	private WifiLock wifiLock;
	private WifiScanner wifiScanner;
	int wakeupMode;
	// private FileLogger locationLogger;
	/**
	 * Scan interval in milliseconds.
	 */
	private long scanInterval;

	public WifiSensor(Context context, UiHandler uiHandler, int wakeupMode) {
		this.context = context;
		this.uiHandler = uiHandler;
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		this.wakeupMode = wakeupMode;
		this.wifiLock = wifiManager.createWifiLock(
				wakeupMode, TAG);
		wifiLock.setReferenceCounted(false);
		this.wifiScanner = new WifiScanner(wifiLock, this);
		this.timer = new PeriodicExecutor(context, wifiScanner);
		// this.locationLogger = null;
	}

	public void start(int interval) {
		uiHandler.appendToTerminal("Wifi measuring started.");
		scanInterval = interval * 1000;
		wifiManager.setWifiEnabled(true);
		context.registerReceiver(this, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		// try {
		// locationLogger = new FileLogger(LOG_NAME);
		// } catch (IOException ex) {
		// uiHandler.appendToTerminal("!!!Cannot open log file!!!");
		// }
		timer.start(scanInterval, true);
	}

	public void startScan() {
		wifiManager.startScan();
		String message = String.valueOf(System.currentTimeMillis()) + "\t"
				+ "Start Scan";
		uiHandler.appendToTerminal(message);
		// try {
		// locationLogger.append(message);
		// } catch (IOException e) {
		// uiHandler
		// .appendToTerminal("!!!Failed to log this new Wifi ScanResult!!!");
		// }
	}

	public void stop() {
		uiHandler.appendToTerminal("Wifi measuring stopped.");
		// if (locationLogger != null) {
		// try {
		// locationLogger.close();
		// } catch (IOException ex) {
		// uiHandler.appendToTerminal("!!!Cannot close log file!!!");
		// }
		// }
		wifiLock.release();
		timer.stop();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		List<ScanResult> wifiResult = wifiManager.getScanResults();
		wifiLock.release();
		// String msg = String.valueOf(System.currentTimeMillis()) + "\t"
		// + String.valueOf(wifiResult.size());
		uiHandler.appendToTerminal("# of in-range APs: " + wifiResult.size());
		// try {
		// locationLogger.append(msg);
		// } catch (IOException e) {
		// uiHandler
		// .appendToTerminal("!!!Failed to log this new Wifi ScanResult!!!");
		// }
	}

}
