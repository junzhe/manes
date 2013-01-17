package org.whispercomm.manes.exp.locationsensor.location.sensor;

import org.whispercomm.manes.exp.locationsensor.location.actuator.SensorActuator;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;

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

	public void startPeriodicMeasures(long peirod);

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
