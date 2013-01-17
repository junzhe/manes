package org.whispercomm.manes.exp.locationsensor.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.whispercomm.manes.exp.locationsensor.data.Accelerometer;
import org.whispercomm.manes.exp.locationsensor.data.CDMA;
import org.whispercomm.manes.exp.locationsensor.data.GPS;
import org.whispercomm.manes.exp.locationsensor.data.GSM;
import org.whispercomm.manes.exp.locationsensor.data.Gyro;
import org.whispercomm.manes.exp.locationsensor.data.GyroFactory;
import org.whispercomm.manes.exp.locationsensor.data.Light;
import org.whispercomm.manes.exp.locationsensor.data.LightFactory;
import org.whispercomm.manes.exp.locationsensor.data.MagnetField;
import org.whispercomm.manes.exp.locationsensor.data.MagnetFieldFactory;
import org.whispercomm.manes.exp.locationsensor.data.WifiDirectGroup;
import org.whispercomm.manes.exp.locationsensor.data.Wifis;
import org.whispercomm.manes.exp.locationsensor.http.HttpManager;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.sensor.AccelerometerSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.CdmaSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.GpsSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.GsmSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.GyroSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.LightSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.LocationUpdater;
import org.whispercomm.manes.exp.locationsensor.location.sensor.MagnetFieldSensor;
import org.whispercomm.manes.exp.locationsensor.location.sensor.WifiDirectGroupCreator;
import org.whispercomm.manes.exp.locationsensor.location.sensor.WifiSensor;
import org.whispercomm.manes.exp.locationsensor.network.IdManager;
import org.whispercomm.manes.exp.locationsensor.network.ManesService;

/**
 * This class manages the sensing and reporting of location information, i.e.,
 * GPS, Wifi, and cell information observed on the device.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
@SuppressLint("SdCardPath")
public class ManesLocationManager {

	public final static String SERVER_URL = ManesService.SERVER_URL;
	public static final String TAG = ManesLocationManager.class.getSimpleName();
	private Context context;
	private GpsSensor gpsSensor;
	private LocationUpdater<GPS> gpsUpdater;
	private WifiSensor wifiSensor;
	private LocationUpdater<Wifis> wifiUpdater;
	private CdmaSensor cdmaSensor;
	private LocationUpdater<CDMA> cdmaUpdater;
	private GsmSensor gsmSensor;
	private LocationUpdater<GSM> gsmUpdater;
	private AccelerometerSensor accSensor;
	private LocationUpdater<Accelerometer> accUpdater;
	private GyroSensor gyroSensor;
	private LocationUpdater<Gyro> gyroUpdater;
	private MagnetFieldSensor magnetSensor;
	private LocationUpdater<MagnetField> magnetUpdater;
	private LightSensor lightSensor;
	private LocationUpdater<Light> lightUpdater;
	private WifiDirectGroupCreator wifiDirectGroupCreator;
	private LocationUpdater<WifiDirectGroup> wifiDirectUpdater;
	private SensorOperator operator;
	private boolean isStarted;

	public ManesLocationManager(Looper looper, HttpManager httpManager,
			IdManager idManager, Context context) {
		this.context = context;
		this.isStarted = false;
		prepareSensors(looper, httpManager, idManager);
		prepareUpdaters(httpManager, idManager, context);
		prepareOperators();
	}

	private void prepareSensors(Looper looper, HttpManager httpManager,
			IdManager idManager) {
		this.gpsSensor = new GpsSensor(this.context);
		this.wifiSensor = new WifiSensor(this.context);
		this.cdmaSensor = new CdmaSensor(this.context);
		this.gsmSensor = new GsmSensor(this.context);
		SensorManager sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		Handler handler = new Handler();
		this.accSensor = new AccelerometerSensor(context, handler,
				sensorManager);
		this.gyroSensor = new GyroSensor(context, handler, sensorManager,
				new GyroFactory());
		this.magnetSensor = new MagnetFieldSensor(context, handler,
				sensorManager, new MagnetFieldFactory());
		this.lightSensor = new LightSensor(context, handler, sensorManager,
				new LightFactory());
		this.wifiDirectGroupCreator = new WifiDirectGroupCreator(this.context,
				looper);
	}

	private void prepareUpdaters(HttpManager httpManager, IdManager idManager,
			Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.gpsUpdater = new LocationUpdater<GPS>(context, httpManager,
				idManager, connManager, "gps", GPS.class);
		this.wifiUpdater = new LocationUpdater<Wifis>(context, httpManager,
				idManager, connManager, "wifi", Wifis.class);
		this.cdmaUpdater = new LocationUpdater<CDMA>(context, httpManager,
				idManager, connManager, "cdma", CDMA.class);
		this.gsmUpdater = new LocationUpdater<GSM>(context, httpManager,
				idManager, connManager, "gsm", GSM.class);
		this.accUpdater = new LocationUpdater<Accelerometer>(context,
				httpManager, idManager, connManager, "accelerometer",
				Accelerometer.class);
		this.gyroUpdater = new LocationUpdater<Gyro>(context, httpManager,
				idManager, connManager, "gyro", Gyro.class);
		this.magnetUpdater = new LocationUpdater<MagnetField>(context,
				httpManager, idManager, connManager, "magnet",
				MagnetField.class);
		this.lightUpdater = new LocationUpdater<Light>(context, httpManager,
				idManager, connManager, "light", Light.class);
		this.wifiDirectUpdater = new LocationUpdater<WifiDirectGroup>(context,
				httpManager, idManager, connManager, "wifidirect",
				WifiDirectGroup.class);

	}

	private void prepareOperators() {
		this.operator = new GeneralOperator(context, this, gpsSensor,
				wifiSensor, cdmaSensor, gsmSensor, accSensor, gyroSensor,
				magnetSensor, lightSensor, wifiDirectGroupCreator, gpsUpdater,
				wifiUpdater, cdmaUpdater, gsmUpdater, accUpdater, gyroUpdater,
				magnetUpdater, lightUpdater, wifiDirectUpdater);
		gpsSensor.setOperator(operator);
		wifiSensor.setOperator(operator);
		accSensor.setOperator(operator);
		gyroSensor.setOperator(operator);
		magnetSensor.setOperator(operator);
		lightSensor.setOperator(operator);
		wifiDirectGroupCreator.setOperator(operator);
	}

	/**
	 * This function should be called in ManesSevice
	 */
	public synchronized void start() {
		if (isStarted)
			return;
		isStarted = true;
		Log.i(TAG, "Starting location manager...");
		startSensors();
		startUpdaters();
		Log.i(TAG, "Location manager successfully started!");
	}

	/**
	 * Should be called when you want to stop the location manager.
	 */
	public synchronized void stop() {
		if (isStarted == false)
			return;
		isStarted = false;
		stopUpdaters();
		stopSensors();
	}

	private void startSensors() {
		gpsSensor.startSensing();
		wifiSensor.startSensing();
		cdmaSensor.startSensing();
		gsmSensor.startSensing();
		accSensor.startSensing();
		gyroSensor.startSensing();
		magnetSensor.startSensing();
		lightSensor.startSensing();
		wifiDirectGroupCreator.startSensing();
	}

	private void stopSensors() {
		wifiDirectGroupCreator.stopSensing();
		lightSensor.stopSensing();
		magnetSensor.stopSensing();
		gyroSensor.stopSensing();
		accSensor.stopSensing();
		Log.i(TAG, "***Stopping gsmSensor...");
		gsmSensor.stopSensing();
		Log.i(TAG, "GSM sensor stopped!");
		Log.i(TAG, "***Stopping cdmaSensor...");
		cdmaSensor.stopSensing();
		Log.i(TAG, "CDMA sensor stopped!");
		Log.i(TAG, "***Stopping wifiSensor...");
		wifiSensor.stopSensing();
		Log.i(TAG, "Wifi sensor stopped!");
		Log.i(TAG, "***Stopping gpsSensor...");
		gpsSensor.stopSensing();
		Log.i(TAG, "GPS sensor stopped!");
	}

	private void startUpdaters() {
		gpsUpdater.startSensing();
		wifiUpdater.startSensing();
		cdmaUpdater.startSensing();
		gsmUpdater.startSensing();
		accUpdater.startSensing();
		gyroUpdater.startSensing();
		magnetUpdater.startSensing();
		lightUpdater.startSensing();
		wifiDirectUpdater.startSensing();
	}

	private void stopUpdaters() {
		wifiDirectUpdater.stopSensing();
		lightUpdater.stopSensing();
		magnetUpdater.stopSensing();
		gyroUpdater.stopSensing();
		gyroUpdater.stopSensing();
		accUpdater.stopSensing();
		gsmUpdater.stopSensing();
		cdmaUpdater.stopSensing();
		wifiUpdater.stopSensing();
		gpsUpdater.stopSensing();
	}
}
