/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.whispercomm.manes.client.macentity.location.operator;

import android.os.Handler;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;

/**
 * This sensor operator stops GPS while we do not get fixed GPS signal (i.e.,
 * indoor), and re-starts GPS when we do not get strong Wifi signal (very likely
 * outdoor);
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
public class WifiGpsAssistedOperator implements SensorOperator {

	private final GpsSensor gpsSensor;
	private final WifiSensor wifiSensor;
	private final Handler mainHandler;

	public WifiGpsAssistedOperator(GpsSensor gpsSensor, WifiSensor wifiSensor,
			Handler mainHandler) {
		this.gpsSensor = gpsSensor;
		this.wifiSensor = wifiSensor;
		this.mainHandler = mainHandler;
	}

	public void inform(SensorSignal signal) {
		// Do nothing if GPS works normally.
		if (gpsSensor.isFixed() && gpsSensor.isSensing() == true) {
			return;
		}
		if (shouldStopGps() == true) {
			mainHandler.post(new Runnable() {

				public void run() {
					gpsSensor.stopSensing();
				}

			});
		} else if (shouldStartGps() == true) {
			mainHandler.post(new Runnable() {

				public void run() {
					gpsSensor.startSensing();
				}

			});
		}
	}

	/**
	 * Stop GPS if it is open but we do not get fixed signal.
	 * 
	 * @return
	 */
	private boolean shouldStopGps() {
		boolean result = (gpsSensor.isFixed() == false)
				&& (gpsSensor.isSensing() == true);
		return result;
	}

	/**
	 * Start GPS if it is closed and Wifi signal becomes weak.
	 * 
	 * @return
	 */
	private boolean shouldStartGps() {
		boolean result = (gpsSensor.isSensing() == false);
		result = result
				&& (WifiOperatorPolicy.isWifiStrong(wifiSensor
						.getLatestReading()) == false);
		return result;
	}
}
