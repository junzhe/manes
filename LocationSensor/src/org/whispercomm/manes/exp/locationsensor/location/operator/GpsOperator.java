package org.whispercomm.manes.exp.locationsensor.location.operator;

import org.whispercomm.manes.exp.locationsensor.data.GPS;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.exp.locationsensor.location.sensor.GpsSensor;

import android.util.Log;

/**
 * This operator is in charge of scheduling new GPS measurement based on inputs
 * of sensor readings, e.g., motion sensor and GPS sensor itself.
 * 
 * @author Yue Liu
 * 
 */
public class GpsOperator implements SensorOperator {

	public static final String TAG = GpsOperator.class.getSimpleName();

	/**
	 * GPS measure period when the device is static, in milliseconds.
	 */
	public static final long GPS_MEASURE_INTERVAL_STATIC = 5 * 60 * 1000;
	public static final long GPS_MEASURE_INTERVAL_INIT = 30 * 1000;
	public static final long GPS_MEASURE_INTERVAL_MIN = 60 * 1000;
	/**
	 * The accuracy of GPS measurement in meters.
	 */
	public static final int GPS_ACCURACY = 5;
	/**
	 * The GPS sensing granularity (in meters) that we achieves.
	 */
	public static final int GPS_SENSING_DISTANCE_STEP = 20;
	public static final GPS GPS_INVALID = new GPS("0", -1, -1);

	private GPS gpsCrt;
	private final GpsSensor gpsSensor;

	public GpsOperator(GpsSensor gpsSensor) {
		this.gpsSensor = gpsSensor;
		this.gpsCrt = null;
	}

	@Override
	synchronized public void inform(SensorSignal signal) {
		if (signal == SensorSignal.GPS_AVAILABLE) {
			// Schedule next measure when the result of current measure returns.
			gpsCrt = gpsSensor.getLatestReading();
			if (gpsCrt == null) {
				gpsCrt = GPS_INVALID;
			}
		}
	}

	/**
	 * Whether we got valid GPS signal last time we measured it.
	 * 
	 * @return
	 */
	public boolean gotGpsSignalLastTime() {
		Log.i(TAG, "***Deciding whether got valid GPS last time...");
		if (gpsCrt == null) {
			// No last measurement. Assume true here.
			Log.i(TAG, "No last measurement. Did not got GPS last time.");
			return true;
		}
		if (gpsCrt.isDataTheSame(GPS_INVALID)) {
			Log.i(TAG, "Did not got GPS last time.");
			return false;
		} else {
			Log.i(TAG, "Got GPS last time.");
			return true;
		}
	}
}
