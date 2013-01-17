package org.whispercomm.manes.exp.wifimeasurer;

import android.net.wifi.WifiManager.WifiLock;

public class WifiScanner implements Runnable {

	private WifiLock wifiLock;
	private WifiSensor wifiSensor;
	
	public WifiScanner(WifiLock wifiLock, WifiSensor wifiSensor){
		this.wifiLock = wifiLock;
		this.wifiSensor = wifiSensor;
	}
	
	@Override
	public void run() {
		wifiLock.acquire();
		wifiSensor.startScan();
	}

}
