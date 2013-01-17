package org.whispercomm.manes.client.macentity.location.actuator;

import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;

/**
 * This interface provides methods to actuate sensor measurements.
 * 
 * @author Yue Liu
 * 
 */
public interface SensorActuator {

	/**
	 * Start one measure of the sensor at the specified time.
	 * 
	 * @param execTime
	 *            the execution time of the measurement.
	 */
	public void startOneMeasureBy(long execTime);

	/**
	 * Makes sure there is one measure before the specified time.
	 * 
	 * @param execTime
	 *            the execution time of the measurement.
	 */
	public void startOneMeasureAt(long eexcTime);

	/**
	 * Shut the actuator, i.e., stop all currently executing and further
	 * to-be-executed measures. This actuator cannot be used anymore.
	 */
	public void shutDown();

	/**
	 * Set the operator for the current actuator.
	 * 
	 * @param operator
	 */
	public void setOperator(SensorOperator operator);
}
