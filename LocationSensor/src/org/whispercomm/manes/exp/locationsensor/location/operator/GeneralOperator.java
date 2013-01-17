package org.whispercomm.manes.exp.locationsensor.location.operator;

import org.whispercomm.manes.exp.locationsensor.data.Accelerometer;
import org.whispercomm.manes.exp.locationsensor.data.CDMA;
import org.whispercomm.manes.exp.locationsensor.data.GPS;
import org.whispercomm.manes.exp.locationsensor.data.GSM;
import org.whispercomm.manes.exp.locationsensor.data.Gyro;
import org.whispercomm.manes.exp.locationsensor.data.Light;
import org.whispercomm.manes.exp.locationsensor.data.MagnetField;
import org.whispercomm.manes.exp.locationsensor.data.WifiDirectGroup;
import org.whispercomm.manes.exp.locationsensor.data.Wifis;
import org.whispercomm.manes.exp.locationsensor.location.ManesLocationManager;
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

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * This {@link SensorOperator} is a central operator that directly takes in all
 * signals from various {@link LocationSensor}s and de-multiplex them to
 * specific operators.
 * 
 * @author Yue Liu
 * 
 */
@SuppressLint("SdCardPath")
public class GeneralOperator implements SensorOperator {

	/**
	 * This "enum" defines various signals that can be handled by various
	 * {@link SensorOperators}.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public enum SensorSignal {
		/**
		 * Notice for GPS measure results available.
		 */
		GPS_AVAILABLE, FAKE_GPS_AVAILABLE,
		/**
		 * Notice for Wifi scan results available.
		 */
		WIFI_AVAILABLE, ACC_AVAILABLE, MAGNET_AVAILABLE, GYRO_AVAILABLE, LIGHT_AVAILABLE, WIFI_DIRECT_AVAILABLE
	}

	public static final String TAG = GeneralOperator.class.getSimpleName();

	private GpsOperator gpsOperator;
	private WifiOperator wifiOperator;
	private UpdateOperator updateOperator;

	public GeneralOperator(Context context,
			ManesLocationManager manesLocManager, GpsSensor gpsSensor,
			WifiSensor wifiSensor, CdmaSensor cdmaSensor, GsmSensor gsmSensor,
			AccelerometerSensor accSensor, GyroSensor gyroSensor,
			MagnetFieldSensor magnetSensor, LightSensor lightSensor,
			WifiDirectGroupCreator wifiDirectCreator,
			LocationUpdater<GPS> gpsUpdater,
			LocationUpdater<Wifis> wifiUpdater,
			LocationUpdater<CDMA> cdmaUpdater, LocationUpdater<GSM> gsmUpdater,
			LocationUpdater<Accelerometer> accUpdater,
			LocationUpdater<Gyro> gyroUpdater,
			LocationUpdater<MagnetField> magnetUpdater,
			LocationUpdater<Light> lightUpdater,
			LocationUpdater<WifiDirectGroup> wifiDirectUpdater) {
		this.gpsOperator = new GpsOperator(gpsSensor);
		this.wifiOperator = new WifiOperator(wifiSensor);
		this.updateOperator = new UpdateOperator(gpsSensor, wifiSensor,
				cdmaSensor, gsmSensor, accSensor, gyroSensor, magnetSensor,
				lightSensor, wifiDirectCreator, gpsUpdater, wifiUpdater,
				cdmaUpdater, gsmUpdater, accUpdater, gyroUpdater,
				magnetUpdater, lightUpdater, wifiDirectUpdater);
	}

	@Override
	public void inform(SensorSignal signal) {
		gpsOperator.inform(signal);
		wifiOperator.inform(signal);
		updateOperator.inform(signal);
	}

	/**
	 * This will decide not to initiate a GPS measurement if the last one did
	 * not catch valid signal and the Wifi environment has not shown significant
	 * change since that. This also carries the indication that the GPS
	 * measurement is the same as last time.
	 * 
	 * @return
	 */
	public boolean shouldNotInitiateNewGpsMeasure() {
		if (gpsOperator.gotGpsSignalLastTime() == false
				&& wifiOperator.gotWifiSignalSinceLastGps() == true
				&& wifiOperator.hasWifiChangedSinceLastGps() == false)
			return true;
		else
			return false;
	}
}
