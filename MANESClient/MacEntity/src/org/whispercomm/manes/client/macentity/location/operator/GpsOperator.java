package org.whispercomm.manes.client.macentity.location.operator;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.util.BoundedFifo;
import org.whispercomm.manes.topology.location.GPS;

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
	 * The number of historical sensor readings stored in this operator.
	 */
	private static final int SENSOR_HISTORY_SIZE = 2;
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
	public static final GPS GPS_INVALID = new GPS(-1, -1);

	private final GpsSensor gpsSensor;
	private BoundedFifo<Long> sensorTimes;
	private BoundedFifo<GPS> sensorReadings;
	private GPS gpsCrt;

	public GpsOperator(GpsSensor gpsSensor) {
		this.gpsSensor = gpsSensor;
		this.sensorTimes = new BoundedFifo<Long>(SENSOR_HISTORY_SIZE);
		this.sensorReadings = new BoundedFifo<GPS>(SENSOR_HISTORY_SIZE);
		this.gpsCrt = null;
	}

	@Override
	synchronized public void inform(SensorSignal signal) {
		long crtTime = System.currentTimeMillis();
		if (signal == SensorSignal.GPS_ACTUATION) {
			// sensorTimes.add(crtTime);
		}
		if (signal == SensorSignal.WIFI_TURNED_WEAK) {
			Log.i(TAG,
					"Start an immediate GPS measure because Wifi turned weak.");
			gpsSensor.startOneMeasureAt(crtTime);
		}
		if (signal == SensorSignal.GPS_AVAILABLE
				|| signal == SensorSignal.FAKE_GPS_AVAILABLE) {
			sensorTimes.add(crtTime);
			// Schedule next measure when the result of current measure returns.
			gpsCrt = gpsSensor.getLatestReading();
			if (gpsCrt == null) {
				gpsCrt = GPS_INVALID;
			}
			// Update history when we get new sensor readings back.
			sensorReadings.add(gpsCrt);
			// Schedule next sensing
			scheduleNextMeasure();
		}
	}

	/**
	 * Whether we got valid GPS signal last time we measured it.
	 * 
	 * @return
	 */
	public boolean gotGpsSignalLastTime() {
		if (gpsCrt == null)
			// No last measurement. Assume true here.
			return true;
		if (gpsCrt.isDataTheSame(GPS_INVALID))
			return false;
		else
			return true;
	}

	/**
	 * Schedule next GPS measure according to the sensing history.
	 */
	private void scheduleNextMeasure() {
		long crtTime = System.currentTimeMillis();
		int size = sensorTimes.size();
		if (size < 2) {
			// not enough history to make decision. Schedule next sensing
			// quickly.
			gpsSensor.startOneMeasureAt(crtTime + GPS_MEASURE_INTERVAL_INIT);
			return;
		}
		// the last sensing time
		long time1 = sensorTimes.get(size - 1).longValue();
		// the second last sensing time
		long time2 = sensorTimes.get(size - 2).longValue();
		long timeInterval = time1 - time2;
		// the last GPS reading
		GPS gps1 = sensorReadings.get(size - 1);
		// the second last GPS reading
		GPS gps2 = sensorReadings.get(size - 2);
		double distance = GPS.getDistance(gps1, gps2);
//		Log.i(TAG, "Loc1, lat: " + gps1.getLat() + ", lon: " + gps1.getLon());
//		Log.i(TAG, "Loc1, lat: " + gps2.getLat() + ", lon: " + gps2.getLon());
//		Log.i(TAG, "The distance between previous location: " + distance);
//		Log.i(TAG, "the time betweeen previous location: " + timeInterval);
		if (timeInterval < GPS_MEASURE_INTERVAL_INIT) {
			// not enough time has passed since last measure, cannot make a
			// reasonable decision. Schedule a safe-guard measure in far future.
			gpsSensor.startOneMeasureBy(crtTime + GPS_MEASURE_INTERVAL_STATIC);
			return;
		}
		if (gps1.isDataTheSame(GPS_INVALID)) {
			// did not get a valid GPS this time. Assuming that we'll in this
			// situation for a long time and schedule next measurement late.
			gpsSensor.startOneMeasureAt(crtTime + GPS_MEASURE_INTERVAL_STATIC);
			return;
		}
		if (distance < GPS_ACCURACY) {
			// not enough distance between the last two measurements. Schedule
			// next sensing with largest delay if there is no earlier ones.
			gpsSensor.startOneMeasureAt(crtTime + GPS_MEASURE_INTERVAL_STATIC);
			return;
		}
		/**
		 * The time that takes the device to stray
		 * {@code GPS_SENSING_DISTANCE_STEP} away from the current location,
		 * estimated according to the estimated speed based on last two
		 * measurements.
		 */
		long delay = (long) (((double) GPS_SENSING_DISTANCE_STEP) / (distance / (double) timeInterval));
		if (delay < GPS_MEASURE_INTERVAL_MIN)
			delay = GPS_MEASURE_INTERVAL_MIN;
		if (delay > GPS_MEASURE_INTERVAL_STATIC)
			delay = GPS_MEASURE_INTERVAL_STATIC;
		gpsSensor.startOneMeasureAt(crtTime + delay);
	}
}
