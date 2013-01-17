package org.whispercomm.manes.client.macentity.location.sensor;

import org.whispercomm.manes.client.macentity.location.actuator.SensorActuator;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;

/**
 * Interface that abstracts the functionalities of all location sensors, i.e.,
 * GPS, Wifi, and cell sensors.
 * 
 * @author Yue Liu
 * @param <T>
 *            the class represents the sensor's measurement results.
 */
public interface LocationSensor<T> {

	/**
	 * Start this sensor.
	 */
	public void startSensing();

	/**
	 * Stop this sensor.
	 */
	public void stopSensing();

	/**
	 * Start one sensor measurement at the desired time.
	 * 
	 * @param execTime
	 *            the desired execution time of the measurement.
	 */
	public void startOneMeasureBy(long execTime);

	/**
	 * Makes sure one sensor measurement will be started before the specified
	 * deadline.
	 * 
	 * @param execTime
	 *            the execution deadline.
	 */
	public void startOneMeasureAt(long eexcTime);

	/**
	 * This method is called by corresponding {@link SensorActuator} to update
	 * the sensor readings.
	 * 
	 * @param newReadings
	 */
	public void updateReadings(T newReadings);

	/**
	 * Get the latest sensor measurement.
	 * 
	 * @return an object that represents the sensor measurements.
	 */
	public T getLatestReading();

	/**
	 * Whether the sensor is currently working.
	 * 
	 * @return
	 */
	public boolean isSensing();

	/**
	 * Set a {@link SensorOperator} for this sensor.
	 * 
	 * @param operator
	 */
	public void setOperator(SensorOperator operator);
}
