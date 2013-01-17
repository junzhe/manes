package org.whispercomm.manes.exp.locationsensor.location.actuator;

import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.sensor.GpsSensor;
import org.whispercomm.manes.exp.locationsensor.util.PeriodicExecutor;
import org.whispercomm.manes.exp.locationsensor.util.TimedExecutor;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * This class provides interface to actuate GPS measurements.
 * 
 * @author Yue Liu
 * 
 */
public class GpsActuator implements GpsStatus.Listener, LocationListener,
		SensorActuator {

	public static final String TAG = GpsActuator.class.getSimpleName();
	/**
	 * The maximum time that GPS searches for a fix.
	 * <p>
	 * CPU will be turned off after this time, regardless whether or not we get
	 * valid GPS signal. This timeout is necessary because we may never get
	 * valid GPS signal, e.g. when we are indoor, and would not want to keep CPU
	 * awake all the time.
	 */
	public static final long GPS_WAIT_TIME_MAX = 30000;
	/**
	 * The maximum time that GPS searches for a first satellite signal.
	 */
	public static final long GPS_SATELLITE_WAIT_TIME_MAX = 10000;

	private final LocationManager locationManager;
	private final GpsSensor gpsSensor;
	private final WakeLock wakeLock;
	private PeriodicExecutor measureExecutor;
	private TimedExecutor stopperExecutor;
	private GpsMeasurer gpsMeasurer;
	private GpsMeasureStopper gpsMeasureStopper;
	private boolean isMeasuring;
	private boolean isStarted;
	private String stopperId;
	private long initTime;
	private LooperThread looperThread;
	private GeneralOperator operator;

	public GpsActuator(Context context, GpsSensor gpsSensor,
			GeneralOperator operator) {
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		this.gpsSensor = gpsSensor;
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG
				+ ".wakelock");
		this.gpsMeasurer = new GpsMeasurer();
		this.gpsMeasureStopper = new GpsMeasureStopper();
		this.measureExecutor = new PeriodicExecutor(context, TAG
				+ ".gpsmeasure", gpsMeasurer);
		this.stopperExecutor = new TimedExecutor(context);
		this.isMeasuring = false;
		this.isStarted = false;
		this.looperThread = new LooperThread();
		looperThread.start();
		this.operator = operator;
	}

	@Override
	synchronized public boolean startPeriodicMeasures(long period) {
		if (isStarted)
			return false;
		measureExecutor.start(period, true);
		isStarted = true;
		return true;
	}

	@Override
	synchronized public boolean stopPeriodicMeasures() {
		if (isStarted == false)
			return false;
		measureExecutor.stop();
		isStarted = false;
		return true;
	}

	private void measure() {
		// Do not start an GPS measuring if there is one already in
		// progress.
		if (isMeasuring) {
			return;
		}
		if (operator != null) {
			Log.i(TAG,
					"***Deciding whether it is necessary to start measuring GPS...");
			if (operator.shouldNotInitiateNewGpsMeasure()) {
				operator.inform(SensorSignal.FAKE_GPS_AVAILABLE);
				return;
			}
		} else {
			Log.i(TAG, "***GPS operator is null...");
		}
		Log.i(System.currentTimeMillis() + "\t" + TAG, "Start a GPS measure!");
		// Set isMeasuring and initiatedMeasureNum
		isMeasuring = true;
		initTime = System.currentTimeMillis();
		// register listener for GPS status
		locationManager.addGpsStatusListener(this);
		// register broadcast receiver for location updates.
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		// schedule GpsMeasureStopper
		stopperId = stopperExecutor.schedule(System.currentTimeMillis()
				+ GPS_SATELLITE_WAIT_TIME_MAX, gpsMeasureStopper);
	}

	/**
	 * Update {@link GpsSensor} with new GPS result, and then stop the current
	 * measurement.
	 * 
	 * @param locationUpdate
	 *            location update from this measurement.
	 */
	private void stopMeasure(Location locationUpdate) {
		Log.i(System.currentTimeMillis() + "\t" + TAG,
				"GPS measure results available!");
		// stop measuring
		locationManager.removeGpsStatusListener(this);
		locationManager.removeUpdates(this);
		// set isMeasuring
		isMeasuring = false;
		// update GpsSensor here only when the measure result is null.
		if (locationUpdate == null) {
			gpsSensor.updateReadings(null);
		}
	}

	@Override
	public void onGpsStatusChanged(int event) {
		Log.i(System.currentTimeMillis() + TAG, "GPS status changed!");
		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			GpsStatus gpsStatus = locationManager.getGpsStatus(null);
			if (gpsStatus.getSatellites().iterator().hasNext()) {
				// got at least one GPS satellite
				Log.i(System.currentTimeMillis() + TAG,
						"Received at least one Satellite signal!");
				locationManager.removeGpsStatusListener(this);
				long crtTime = System.currentTimeMillis();
				stopperExecutor.cancel(stopperId);
				stopperId = stopperExecutor.schedule(crtTime
						+ GPS_WAIT_TIME_MAX - (crtTime - initTime),
						gpsMeasureStopper);
			}
		}
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = (GeneralOperator) operator;
	}

	@Override
	public void shutDown() {
		// shut down the looper thread.
		Log.i(TAG, "***Shutting down looper thread!");
		looperThread.looperHandler.getLooper().quit();
		Log.i(TAG, "***looper shut down!");
		try {
			looperThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		Log.i(TAG, "***looper thread is shut down!");
		Log.i(TAG, "***Shutting down measureExecutor...");
		measureExecutor.stop();
		isStarted = false;
		Log.i(TAG, "***measureExecutor shut down!");
		// Stop measure in case necessary stoppers are canceled during the
		// previous shutdown process.
		stopMeasure(null);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Log.i(System.currentTimeMillis() + TAG, "Got a GPS fix!");
			if (stopperExecutor.hasPendingJob(stopperId)) {
				Log.i(System.currentTimeMillis() + TAG,
						"Cancel corresponding stopper!");
				// cancel the corresponding GpsMeasurerStopper scheduled
				// for GPS_WAIT_TIME_MAX, if it still exists.
				stopperExecutor.cancel(stopperId);
				// stop current GPS measurement now.
				stopMeasure(location);
			}
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// Do nothing here.
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// Do nothing here.
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// Do nothing here.
	}

	/**
	 * This subclass of {@link Thread} runs as a {@link Looper} thread.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class LooperThread extends Thread {
		public Handler looperHandler;

		@Override
		public void run() {
			Looper.prepare();
			this.looperHandler = new Handler();
			Looper.loop();
		}
	}

	/**
	 * Measure the device's current GPS location.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public class GpsMeasurer implements Runnable {
		@Override
		public void run() {
			// ask looperThread to run measure().
			looperThread.looperHandler.post(new Runnable() {

				@Override
				public void run() {
					wakeLock.acquire();
					GpsActuator.this.measure();
					wakeLock.release();
				}

			});
		}
	}

	/**
	 * This class provides method to stop a given GPS measurement. It is used to
	 * cancel a GPS measurement in case the measurement has never detected GPS
	 * changes, and prevent we have GPS on for that measurement forever.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public class GpsMeasureStopper implements Runnable {

		@Override
		public void run() {
			// ask LooperThread to stop measure.
			looperThread.looperHandler.post(new Runnable() {

				@Override
				public void run() {
					wakeLock.acquire();
					GpsActuator.this.stopMeasure(null);
					wakeLock.release();
				}

			});
		}

	}
}
