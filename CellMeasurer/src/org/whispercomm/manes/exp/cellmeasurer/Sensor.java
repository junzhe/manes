package org.whispercomm.manes.exp.cellmeasurer;

/**
 * Interface for location sensing.
 *
 * @author Yue Liu
 */
public interface Sensor {

	/**
     * Start the corresponding sensor to measure.
     * @param interval the measure interval in seconds.
     */
    public void start(int interval);

    /**
     * Stop the corresponding sensor's measurement.
     */
    public void stop();
}
