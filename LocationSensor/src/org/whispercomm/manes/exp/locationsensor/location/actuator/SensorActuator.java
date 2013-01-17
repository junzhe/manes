package org.whispercomm.manes.exp.locationsensor.location.actuator;

import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;

/**
 * This interface provides methods to actuate sensor measurements.
 * 
 * @author Yue Liu
 * 
 */
public interface SensorActuator {

	public boolean startPeriodicMeasures(long peirod);
	
	public boolean stopPeriodicMeasures();

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
