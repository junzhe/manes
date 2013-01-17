package org.whispercomm.manes.topology.server.topoestimator;

import java.util.Map;

import org.whispercomm.manes.topology.location.Location;

/**
 * Generate new topology estimation. Different estimation algorithms based on
 * different measures should implement this interface independently .
 * 
 * @author Yue Liu
 * 
 */
public interface TopologyCalculator {

	/**
	 * Estimate topology according to the given location of a client. For the
	 * given client, its topology is presented as a map of its neighbors<->link
	 * quality (i.e., connection probability).
	 * 
	 * Note that if the calculator is unable to estimate the link quality of a
	 * neighbor, the neighbor should not appear on the returned map. For
	 * example, if there is no {@link Wifi} information (i.e.,
	 * Location.getWifi() == null), any calculator that purely relies on
	 * {@link Wifi} information should just return an empty map. That is to say,
	 * the calculator could not estimate the link qualities between this client
	 * and any of its neighbors.
	 * 
	 * @param user_id
	 * @param location
	 * 
	 * @return a HashMap describing the link quality of all neighbors of the
	 *         given client.
	 */
	public Map<Integer, Float> calculate(int user_id, Location location);
}
