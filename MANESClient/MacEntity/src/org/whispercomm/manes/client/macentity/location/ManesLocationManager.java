package org.whispercomm.manes.client.macentity.location;

import android.content.Context;
import android.util.Log;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.location.sensor.CdmaSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GsmSensor;
import org.whispercomm.manes.client.macentity.location.sensor.LocationUpdater;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;
import org.whispercomm.manes.client.macentity.network.IdManager;

/**
 * This class manages the sensing and reporting of location information, i.e.,
 * GPS, Wifi, and cell information observed on the device.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
public class ManesLocationManager {

	public final static String SERVER_URL = "http://topo.api.manes.whispercomm.org:7890";
	public static final String TAG = "org.whispercomm.manes.client.macentity."
			+ "location.LocationManager";
	private Context context;
	private GpsSensor gpsSensor;
	private WifiSensor wifiSensor;
	private CdmaSensor cdmaSensor;
	private GsmSensor gsmSensor;
	private LocationUpdater locationUpdater;
	private SensorOperator operator;

	public ManesLocationManager(HttpManager httpManager, IdManager idManager,
			Context context) {
		this.context = context;
		prepareSensors(httpManager, idManager);
		prepareOperators();
	}

	private void prepareSensors(HttpManager httpManager, IdManager idManager) {
		this.gpsSensor = new GpsSensor(this.context);
		this.wifiSensor = new WifiSensor(this.context);
		this.cdmaSensor = new CdmaSensor(this.context);
		this.gsmSensor = new GsmSensor(this.context);
		TopologyServerSynchronizer synchronizer = new TopologyServerSynchronizer(
				this.cdmaSensor, this.gsmSensor, this.gpsSensor,
				this.wifiSensor);
		LocationSender locationSender = new LocationSender(synchronizer,
				httpManager, idManager);
		this.locationUpdater = new LocationUpdater(context, locationSender,
				synchronizer, gpsSensor, wifiSensor);
	}

	private void prepareOperators() {
		this.operator = new GeneralOperator(context, this, locationUpdater,
				gpsSensor, wifiSensor);
		gpsSensor.setOperator(operator);
		wifiSensor.setOperator(operator);
		locationUpdater.setOperator(operator);
	}

	/**
	 * This function should be called in ManesSevice
	 */
	public synchronized void start() {
		Log.i(TAG, "Starting location handler...");
		startSensors();
		Log.i(TAG, "Location handler successfully started!");
	}

	/**
	 * Should be called when you want to stop the location manager.
	 */
	public synchronized void stop() {
		stopSensors();
	}

	private void startSensors() {
		gpsSensor.startSensing();
		wifiSensor.startSensing();
		cdmaSensor.startSensing();
		gsmSensor.startSensing();
		locationUpdater.startSensing();
	}

	private void stopSensors() {
		gpsSensor.stopSensing();
		Log.i(TAG, "GPS sensor stopped.");
		wifiSensor.stopSensing();
		Log.i(TAG, "Wifi sensor stopped.");
		cdmaSensor.stopSensing();
		Log.i(TAG, "CDMA sensor stopped.");
		gsmSensor.stopSensing();
		Log.i(TAG, "GSM sensor stopped.");
		locationUpdater.stopSensing();
		Log.i(TAG, "locationupdater stopped.");
	}
}
