package org.whispercomm.manes.client.macentity.location.operator;

import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator.SensorSignal;

/**
 * This interface provides the functionalities to actuate/de-actuate various
 * sensors according to different policies.
 * 
 * @author Yue Liu
 */
public interface SensorOperator {

	/**
	 * Notify the operator to check the state of the sensors and make
	 * appropriate operations accordingly.
	 * 
	 * @param signal
	 *            different signal informs different events that need the
	 *            operator to handle.
	 */
	public void inform(SensorSignal signal);
}
