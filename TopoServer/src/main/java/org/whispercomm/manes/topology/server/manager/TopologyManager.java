package org.whispercomm.manes.topology.server.manager;

import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;

/**
 * Manages topology estimation and updates.
 * 
 * @author Yue Liu
 * 
 */
public interface TopologyManager {

	/**
	 * Pre-process the location update from the client, and complete the
	 * network-fetched {@link Location} object with detailed information.
	 * 
	 * @param user_id
	 * @param location
	 *            update from network
	 * @return previous location
	 * @throws LocationUpdateBadException
	 * @throws DataStoreFailureException
	 */
	public Location preprocess(int user_id, Location location)
			throws LocationUpdateBadException, DataStoreFailureException;

	/**
	 * Update trace log with the new location update.
	 * 
	 * @param user_id
	 * @param locationLast
	 *            last location.
	 * @param locationCrt
	 *            current location.
	 */
	public void updateTraceLog(int user_id, Location locationLast,
			Location locationCrt);

	/**
	 * Update topology estimation according to the new location update.
	 * 
	 * @param user_id
	 * @param locationLast
	 *            last location.
	 * @param locationCrt
	 *            current location.
	 */
	public void updateTopology(int user_id, Location locationLast,
			Location locationCrt);

}
