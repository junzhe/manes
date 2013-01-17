package org.whispercomm.manes.client.macentity.location.actuator;

/**
 * This abstract class contains common code of all those that
 * implement {@link SensorActuator}.
 * 
 * @author Yue Liu
 * 
 */
public abstract class AbstractActuator implements SensorActuator {

	public static String TAG = AbstractActuator.class.getSimpleName();

	@Override
	public void startOneMeasureBy(long execTime) {
		// the already scheduled next sensing time
		Long timeNextScheduled = getNextActuationTime();
		if (timeNextScheduled != null) {
			if (timeNextScheduled <= execTime) {
//				Log.i(System.currentTimeMillis() + "\t" + TAG,
//						"next sensor measure is not changed! It will be "
//								+ (timeNextScheduled - System
//										.currentTimeMillis()));
				return;
			}
		}
		startOneMeasureAt(execTime);
//		Log.i(System.currentTimeMillis() + "\t" + TAG,
//				"next sensor measure is scheduled "
//						+ (execTime - System.currentTimeMillis()) + " later!");
	}

	/**
	 * Cancel all pending measures.
	 */
	protected abstract void cancelPendingMeasures();

	/**
	 * Return the next actuation time of the corresponding sensor.
	 * 
	 * @return
	 */
	protected abstract Long getNextActuationTime();

}
