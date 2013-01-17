package org.whispercomm.manes.client.macentity.location.actuator;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.util.TimedExecutor;
import org.whispercomm.manes.topology.location.GPS;

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
public class GpsActuator extends AbstractActuator implements
		GpsStatus.Listener, LocationListener {

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
	private GeneralOperator operator;
	private final WakeLock wakeLock;
	private TimedExecutor measureExecutor;
	private TimedExecutor stopperExecutor;
	private GpsMeasurer gpsMeasurer;
	private GpsMeasureStopper gpsMeasureStopper;
	private boolean isMeasuring;
	private String stopperId;
	private long initTime;
	private LooperThread looperThread;

	public GpsActuator(Context context, GpsSensor gpsSensor,
			GeneralOperator operator) {
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		this.gpsSensor = gpsSensor;
		this.operator = operator;
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG
				+ ".wakelock");
		this.measureExecutor = new TimedExecutor(context);
		this.stopperExecutor = new TimedExecutor(context);
		this.gpsMeasurer = new GpsMeasurer();
		this.gpsMeasureStopper = new GpsMeasureStopper();
		this.isMeasuring = false;
		this.looperThread = new LooperThread();
		looperThread.start();
	}

	/**
	 * This methods allows the caller to start one GPS measurement at the
	 * specified time.
	 * <p>
	 * Note that this method may silently fail if the calling sensor is already
	 * shut down.
	 * 
	 * @param execTime
	 *            the scheduled time of the measurement. This time complies with
	 *            {@code System.currentTimeMillis()} (wall clock time in UTC).
	 *            Note that the job is triggered if this execTime is smaller
	 *            than the current system time.
	 */
	@Override
	public void startOneMeasureAt(long execTime) {
		cancelPendingMeasures();
		measureExecutor.schedule(execTime, gpsMeasurer);
//		Log.i(System.currentTimeMillis() + "\t" + TAG,
//				"next sensor measure is scheduled "
//						+ (execTime - System.currentTimeMillis()) + " later.");
	}

	@Override
	protected Long getNextActuationTime() {
		return measureExecutor.getNextExecTime();
	}

	@Override
	protected void cancelPendingMeasures() {
		measureExecutor.cancelAllPendingJobs();
		// If a measure is already in progress, execute its pending stopper.
		if (stopperExecutor.getNextExecTime() != null) {
			stopperExecutor.cancelAllPendingJobs();
			stopMeasure(null);
		}
	}

	private void measure() {
		// Do not start an GPS measuring if there is one already in
		// progress.
		if (isMeasuring) {
			return;
		}
		// Do not start GPS measurement if the operator tells not to.
		if (operator != null) {
			if (operator.shouldNotInitiateNewGpsMeasure()) {
				operator.inform(SensorSignal.FAKE_GPS_AVAILABLE);
				return;
			}
		}
		Log.i(System.currentTimeMillis() + "\t" + TAG, "Start a GPS measure.");
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
		// notice operator about this actuation
		if (operator != null) {
			operator.inform(SensorSignal.GPS_ACTUATION);
		}
	}

	/**
	 * Update {@link GpsSensor} with new GPS result, and then stop the current
	 * measurement.
	 * 
	 * @param locationUpdate
	 *            location update from this measurement.
	 */
	private void stopMeasure(Location locationUpdate) {
//		Log.i(System.currentTimeMillis() + "\t" + TAG,
//				"GPS measure results available!");
		// stop measuring
		locationManager.removeGpsStatusListener(this);
		locationManager.removeUpdates(this);
		// set isMeasuring
		isMeasuring = false;
		// update GpsSensor.
		GPS gps = null;
		if (locationUpdate != null)
			gps = new GPS(locationUpdate.getLatitude(),
					locationUpdate.getLongitude());
		gpsSensor.updateReadings(gps);
	}

	@Override
	public void onGpsStatusChanged(int event) {
//		Log.i(System.currentTimeMillis() + TAG, "GPS status changed!");
		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			GpsStatus gpsStatus = locationManager.getGpsStatus(null);
			if (gpsStatus.getSatellites().iterator().hasNext()) {
				// got at least one GPS satellite
//				Log.i(System.currentTimeMillis() + TAG,
//						"Received at least one Satellite signal!");
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
//		Log.i(TAG, "***Shutting down looper thread!");
		looperThread.looperHandler.getLooper().quit();
//		Log.i(TAG, "***looper shut down!");
		try {
			looperThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage(), e);
		}
//		Log.i(TAG, "***looper thread is shut down!");
//		Log.i(TAG, "***Shutting down measureExecutor...");
		measureExecutor.shutDown();
//		Log.i(TAG, "***measureExecutor shut down!");
//		Log.i(TAG, "***Shutting down stopperExecutor...");
		stopperExecutor.shutDown();
//		Log.i(TAG, "***stopperExecutor shut down!");
		// Stop measure in case necessary stoppers are canceled during the
		// previous shutdown process.
		stopMeasure(null);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Log.i(System.currentTimeMillis() + TAG, "Got a GPS fix.");
			if (stopperExecutor.hasPendingJob(stopperId)) {
//				Log.i(System.currentTimeMillis() + TAG,
//						"Cancel corresponding stopper!");
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
