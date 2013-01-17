package org.whispercomm.manes.exp.locationsensor.location.operator;

import java.io.FileWriter;
import java.io.IOException;

import org.whispercomm.manes.exp.locationsensor.data.Accelerometer;
import org.whispercomm.manes.exp.locationsensor.data.CDMA;
import org.whispercomm.manes.exp.locationsensor.data.GPS;
import org.whispercomm.manes.exp.locationsensor.data.GSM;
import org.whispercomm.manes.exp.locationsensor.data.Gyro;
import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.Light;
import org.whispercomm.manes.exp.locationsensor.data.MagnetField;
import org.whispercomm.manes.exp.locationsensor.data.OneDimMeasure;
import org.whispercomm.manes.exp.locationsensor.data.ThreeDimMeasure;
import org.whispercomm.manes.exp.locationsensor.data.WifiDirectGroup;
import org.whispercomm.manes.exp.locationsensor.data.Wifis;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
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
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressLint("SdCardPath")
public class UpdateOperator implements SensorOperator {

	public static final String TAG = UpdateOperator.class.getSimpleName();

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
	private ObjectMapper objectMapper;

	public UpdateOperator(GpsSensor gpsSensor, WifiSensor wifiSensor,
			CdmaSensor cdmaSensor, GsmSensor gsmSensor,
			AccelerometerSensor accSensor, GyroSensor gyroSensor,
			MagnetFieldSensor magnetSensor, LightSensor lightSensor,
			WifiDirectGroupCreator wifiDirectGroupCreator,
			LocationUpdater<GPS> gpsUpdater,
			LocationUpdater<Wifis> wifiUpdater,
			LocationUpdater<CDMA> cdmaUpdater, LocationUpdater<GSM> gsmUpdater,
			LocationUpdater<Accelerometer> accUpdater,
			LocationUpdater<Gyro> gyroUpdater,
			LocationUpdater<MagnetField> magnetUpdater,
			LocationUpdater<Light> lightUpdater,
			LocationUpdater<WifiDirectGroup> wifiDirectUpdater) {
		this.gpsSensor = gpsSensor;
		this.wifiSensor = wifiSensor;
		this.cdmaSensor = cdmaSensor;
		this.gsmSensor = gsmSensor;
		this.accSensor = accSensor;
		this.gyroSensor = gyroSensor;
		this.magnetSensor = magnetSensor;
		this.lightSensor = lightSensor;
		this.wifiDirectGroupCreator = wifiDirectGroupCreator;
		this.gpsUpdater = gpsUpdater;
		this.wifiUpdater = wifiUpdater;
		this.cdmaUpdater = cdmaUpdater;
		this.gsmUpdater = gsmUpdater;
		this.accUpdater = accUpdater;
		this.gyroUpdater = gyroUpdater;
		this.magnetUpdater = magnetUpdater;
		this.lightUpdater = lightUpdater;
		this.wifiDirectUpdater = wifiDirectUpdater;
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	private <T> void handleUpdates(T data, String fileName,
			LocationUpdater<T> locUpdater) {
		// log the new sensor reading to file
		try {
			objectMapper.writeValue(new FileWriter(fileName, true), data);
		} catch (JsonGenerationException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (JsonMappingException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		// put the new sensor reading to the updater's buffer
		locUpdater.updateReadings(data);
	}

	@Override
	public void inform(SensorSignal signal) {
		if (signal == SensorSignal.GPS_AVAILABLE
				|| signal == SensorSignal.FAKE_GPS_AVAILABLE) {
			GPS gps = gpsSensor.getLatestReading();
			if (gps == null || signal == SensorSignal.FAKE_GPS_AVAILABLE) {
				gps = new GPS();
				gps.setTime(HumanReadableTime.getCurrentTime());
			}
			handleUpdates(gps, "/sdcard/location-sensor-gps.json", gpsUpdater);
			// cdma
			CDMA cdma = cdmaSensor.getLatestReading();
			if (cdma == null) {
				cdma = new CDMA();
				cdma.setTime(HumanReadableTime.getCurrentTime());
			}
			handleUpdates(cdma, "/sdcard/location-sensor-cdma.json",
					cdmaUpdater);
			// gsm
			GSM gsm = gsmSensor.getLatestReading();
			if (gsm == null) {
				gsm = new GSM();
				gsm.setTime(HumanReadableTime.getCurrentTime());
			}
			handleUpdates(gsm, "/sdcard/location-sensor-gsm.json", gsmUpdater);
		}
		if (signal == SensorSignal.WIFI_AVAILABLE) {
			Wifis wifi = wifiSensor.getLatestReading();
			if (wifi == null) {
				wifi = new Wifis();
				wifi.setTime(HumanReadableTime.getCurrentTime());
			}
			handleUpdates(wifi, "/sdcard/location-sensor-wifi.json",
					wifiUpdater);
		}
		if (signal == SensorSignal.ACC_AVAILABLE) {
			Accelerometer acc = accSensor.getLatestReading();
			if (acc.getMeasures().size() == 0) {
				ThreeDimMeasure point = new ThreeDimMeasure();
				point.setTime(HumanReadableTime.getCurrentTime());
				acc.getMeasures().add(point);
			}
			handleUpdates(acc, "/sdcard/location-sensor-accelerometer.json",
					accUpdater);
		}
		if (signal == SensorSignal.GYRO_AVAILABLE) {
			Gyro gyro = gyroSensor.getLatestReading();
			if (gyro.getMeasures().size() == 0) {
				ThreeDimMeasure point = new ThreeDimMeasure();
				point.setTime(HumanReadableTime.getCurrentTime());
				gyro.getMeasures().add(point);
			}
			handleUpdates(gyro, "/sdcard/location-sensor-gyro.json",
					gyroUpdater);
		}
		if (signal == SensorSignal.MAGNET_AVAILABLE) {
			MagnetField magnet = magnetSensor.getLatestReading();
			if (magnet.getMeasures().size() == 0) {
				ThreeDimMeasure point = new ThreeDimMeasure();
				point.setTime(HumanReadableTime.getCurrentTime());
				magnet.getMeasures().add(point);
			}
			handleUpdates(magnet, "/sdcard/location-sensor-magnet.json",
					magnetUpdater);
		}
		if (signal == SensorSignal.LIGHT_AVAILABLE) {
			Light light = lightSensor.getLatestReading();
			if (light.getMeasures().size() == 0) {
				OneDimMeasure point = new OneDimMeasure();
				point.setTime(HumanReadableTime.getCurrentTime());
				light.getMeasures().add(point);
			}
			handleUpdates(light, "/sdcard/location-sensor-light.json",
					lightUpdater);
		}
		if (signal == SensorSignal.WIFI_DIRECT_AVAILABLE) {
			WifiDirectGroup wifidirect = wifiDirectGroupCreator
					.getLatestReading();
			handleUpdates(wifidirect,
					"/sdcard/location-sensor-wifi-direct.json",
					wifiDirectUpdater);
		}
	}

}
