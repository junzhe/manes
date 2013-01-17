package org.whispercomm.manes.client.macentity.location.actuator;

import org.whispercomm.manes.client.macentity.location.LocationSender;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.util.TimedExecutor;

import android.content.Context;
import android.util.Log;

/**
 * This class provides interfaces to send a location update.
 * 
 * @author Yue Liu
 * 
 */
public class LocationUpdateActuator extends AbstractActuator {

	public static final String TAG = LocationUpdateActuator.class
			.getSimpleName();

	private TimedExecutor executor;
	private LocationSender locationSender;
	private UpdateLocation updateLocation;
	private GeneralOperator operator;

	public LocationUpdateActuator(Context context,
			LocationSender locationSender, GeneralOperator operator) {
		this.executor = new TimedExecutor(context);
		this.locationSender = locationSender;
		this.updateLocation = new UpdateLocation();
		this.operator = operator;
	}

	@Override
	public void startOneMeasureAt(long execTime) {
		cancelPendingMeasures();
		executor.schedule(execTime, updateLocation);
//		Log.i(System.currentTimeMillis() + "\t" + TAG,
//				"next sensor measure is scheduled "
//						+ (execTime - System.currentTimeMillis()) + " later.");
	}

	@Override
	public void shutDown() {
		executor.shutDown();
	}

	@Override
	protected void cancelPendingMeasures() {
		executor.cancelAllPendingJobs();
	}

	@Override
	protected Long getNextActuationTime() {
		return executor.getNextExecTime();
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = (GeneralOperator) operator;
	}

	/**
	 * private wrapper class on {@link LocationSender}. This will schedule the
	 * next location update after posting current location.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class UpdateLocation implements Runnable {

		@Override
		public void run() {
			Log.i(System.currentTimeMillis() + "\t" + TAG,
					"Posting location update.");
			locationSender.postLocation(LocationSender.MORE_DETAIL_RETRY_NUM);
			// notice operator about this actuation
			if (operator != null) {
				operator.inform(SensorSignal.LOC_UPDATE_ACTUATION);
			}
		}

	}

}
