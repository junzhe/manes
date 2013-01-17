package org.whispercomm.manes.topology.server.topoestimator;

import java.util.Map;

import org.whispercomm.manes.topology.location.Location;

/**
 * This interface produces location estimation with given location update.
 * 
 * @author Yue Liu
 * 
 */
public interface TopologyEstimator {

	/**
	 * Decide whether the estimated topology based on the current location
	 * update would be the same as the previous topology estimation.
	 * 
	 * @param locationLast
	 *            the last location update.
	 * @param locationCrt
	 *            the current location update.
	 * @return
	 */
	boolean asPrevious(Location locationLast, Location locationCrt);

	/**
	 * Calculate new topology estimation.
	 * 
	 * @param locationLast
	 * @param locationCrt
	 * @return
	 * @throws EstimationFailureException
	 */
	public Map<Integer, Float> estimate(Location locationLast,
			Location locationCrt) throws EstimationFailureException;

}
