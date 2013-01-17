package org.whispercomm.manes.topology.server.data;

import java.util.Map;
import java.util.Set;

import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;

/**
 * Interface to retrieve and store topology-related data.
 * 
 * @author Yue Liu
 * 
 */
public interface DataInterface {

	/**
	 * Get the user's latest location.
	 * 
	 * @param user_id
	 * @return
	 * @throws DataStoreFailureException
	 */
	public Location getClientLocation(int user_id)
			throws DataStoreFailureException;

	/**
	 * Update user location. This also involves updating a few other stores,
	 * e.e., AP to clients and GPSGrid to clients.
	 * 
	 * @param user_id
	 * @param location
	 * @throws DataStoreFailureException
	 */
	public void updateClientLocation(int user_id, Location location)
			throws DataStoreFailureException;

	/**
	 * Interpret {@code locationRaw} and make it complete, i.e., populate all
	 * "as-previous" fields with previous information. This basically involves a
	 * read-and-then-write operation on the data store.
	 * 
	 * @param user_id
	 * @param locationRaw
	 * @return previous location in database
	 * @throws LocationUpdateBadException
	 * @throws DataStoreFailureException
	 */
	public Location interpretAndThenUpdateClientLocation(int user_id,
			Location locationRaw) throws LocationUpdateBadException,
			DataStoreFailureException;

	/**
	 * Get the AP's latest clients.
	 * 
	 * @param AP_Mac
	 * @return
	 * @throws DataStoreFailureException
	 */

	public Set<Integer> getAPClient(long AP_Mac)
			throws DataStoreFailureException;

	/**
	 * Get the GPSGrid's latest clients, i.e., residents.
	 * 
	 * @param grid
	 * @return
	 * @throws DataStoreFailureException
	 */
	public Set<Integer> getGPSGridClient(GpsGridElement grid)
			throws DataStoreFailureException;

	/**
	 * Get the user's neighbors, and their corresponding link quality.
	 * 
	 * @param user_id
	 * @return
	 * @throws DataStoreFailureException
	 */
	public Map<Integer, Float> getClientNeighbors(int user_id)
			throws DataStoreFailureException;

	/**
	 * Update the user's neighbors. This also involves updating all the
	 * neighbors' topology maps.
	 * 
	 * @param user_id
	 * @param neighbors
	 * @throws DataStoreFailureException
	 */
	public void setClientNeighbors(int user_id, Map<Integer, Float> neighbors)
			throws DataStoreFailureException;

}
