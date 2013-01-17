package org.whispercomm.manes.exp.motionmeasurer;

/**
 * Interface for location sensing.
 *
 * @author Yue Liu
 */
public interface ManesSensor {

	/**
     * Start the corresponding sensor to measure.
     * @param interval the sensing rate.
     */
    public void start(int interval);

    /**
     * Stop the corresponding sensor's measurement.
     */
    public void stop();
}
