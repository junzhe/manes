package org.whispercomm.manes.topology.server.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.expirablestore.serialization.SerializationFailureException;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.server.expirablestore.ExpirableStoreClient;
import org.whispercomm.manes.server.expirablestore.MinimumExpirableUnit;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.NoPreviousDataException;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;
import org.whispercomm.manes.topology.server.logger.UserLogManager;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import voldemort.versioning.ObsoleteVersionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DataInterfaceVoldemort implements DataInterface {

	private final ExpirableStoreClient<Integer, Location, String, MinimumExpirableUnit<Location>, byte[]> clientLocation;
	private final ExpirableStoreClient<Long, Set<Integer>, String, Map<Integer, MinimumExpirableUnit<Integer>>, byte[]> APClient;
	private final ExpirableStoreClient<GpsGridElement, Set<Integer>, byte[], Map<Integer, MinimumExpirableUnit<Integer>>, byte[]> GPSGridClient;
	private final ExpirableStoreClient<Integer, Map<Integer, Float>, String, Map<Integer, MinimumExpirableUnit<Float>>, byte[]> topology;
	private final Logger log;

	@Inject
	public DataInterfaceVoldemort(
			@Named("clientLocation") ExpirableStoreClient<Integer, Location, String, MinimumExpirableUnit<Location>, byte[]> clientLocation,
			@Named("APClient") ExpirableStoreClient<Long, Set<Integer>, String, Map<Integer, MinimumExpirableUnit<Integer>>, byte[]> APClient,
			@Named("GPSGridClient") ExpirableStoreClient<GpsGridElement, Set<Integer>, byte[], Map<Integer, MinimumExpirableUnit<Integer>>, byte[]> GPSGridClient,
			@Named("topology") ExpirableStoreClient<Integer, Map<Integer, Float>, String, Map<Integer, MinimumExpirableUnit<Float>>, byte[]> topology) {
		this.clientLocation = clientLocation;
		this.APClient = APClient;
		this.GPSGridClient = GPSGridClient;
		this.topology = topology;
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public Location getClientLocation(int user_id)
			throws DataStoreFailureException {
		try {
			return clientLocation.get(Integer.valueOf(user_id));
		} catch (SerializationFailureException e1) {
			throw new DataStoreFailureException(e1);
		}
	}

	/**
	 * Do nothing since we never call this function.
	 */
	@Override
	public void updateClientLocation(int user_id, Location location)
			throws DataStoreFailureException {
	}

	/**
	 * Update AP-client store according to the given user's location update.
	 * 
	 * @param user_id
	 * @param location
	 */
	protected void updateAPClientFromLocation(int user_id, Location location) {
		if (location == null)
			return;
		if (location.getWifi() == null)
			return;

		Integer user = Integer.valueOf(user_id);
		Set<Integer> clientSet = new HashSet<Integer>();
		clientSet.add(user);

		List<Wifi> wifis = location.getWifi().getWifi();
		Iterator<Wifi> it = wifis.iterator();
		Wifi wifiCrt;
		Long apCrt;
		while (it.hasNext()) {
			wifiCrt = it.next();
			apCrt = Long.valueOf(wifiCrt.getAp());
			try {
				APClient.update(apCrt, clientSet);
			} catch (SerializationFailureException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Update GPSGrid-client according to the given user's location update.
	 * 
	 * @param user_id
	 * @param location
	 * @throws DataStoreFailureException
	 */
	protected void updateGPSGridClientFromLocation(int user_id,
			Location location) {

		Integer user = Integer.valueOf(user_id);
		Set<Integer> clientSet = new HashSet<Integer>();
		clientSet.add(user);

		GPS gps = location.getGps();
		if (gps == null)
			return;
		GpsGridElement gpsGrid = new GpsGridElement(gps);
		try {
			GPSGridClient.update(gpsGrid, clientSet);
		} catch (SerializationFailureException e) {
			log.error(e.getMessage(), e);
		}

	}

	@Override
	public Set<Integer> getAPClient(long AP_Mac)
			throws DataStoreFailureException {
		try {
			return APClient.get(Long.valueOf(AP_Mac));
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}
	}

	@Override
	public Set<Integer> getGPSGridClient(GpsGridElement grid)
			throws DataStoreFailureException {
		try {
			return GPSGridClient.get(grid);
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}

	}

	@Override
	public Map<Integer, Float> getClientNeighbors(int user_id)
			throws DataStoreFailureException {
		try {
			return topology.get(Integer.valueOf(user_id));
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}

	}

	// TODO all the operations within this function should either succeed
	// together or fail together. However, the current implementation only tries
	// the best for updateNeighborTopology().
	@Override
	public void setClientNeighbors(int user_id, Map<Integer, Float> neighbors)
			throws DataStoreFailureException {
		Integer user = Integer.valueOf(user_id);
		try {
			topology.put(user, neighbors);
		} catch (SerializationFailureException e1) {
			throw new DataStoreFailureException(e1);
		}
		// update topology log
		logTopology(user_id, neighbors);
		// update all the neighbors' topology maps.
		updateNeighborTopology(user, neighbors);
	}

	/**
	 * Update its neighbors' topology when a user update its topology with
	 * neighbors.
	 * 
	 * @param user
	 * @param neighbors
	 */
	protected void updateNeighborTopology(Integer user,
			Map<Integer, Float> neighbors) {
		if (neighbors == null)
			return;
		if (neighbors.size() == 0)
			return;
		Iterator<Integer> it = neighbors.keySet().iterator();
		Integer clientCrt;
		Float linkCrt;
		Map<Integer, Float> neighborsCrt;
		while (it.hasNext()) {
			clientCrt = it.next();
			linkCrt = neighbors.get(clientCrt);
			neighborsCrt = new HashMap<Integer, Float>();
			neighborsCrt.put(user, linkCrt);
			try {
				neighborsCrt = topology.update(clientCrt, neighborsCrt);
			} catch (SerializationFailureException e) {
				log.error(e.getMessage(), e);
			}
			// update topology log
			logTopology(clientCrt, neighborsCrt);
		}
	}

	@Override
	public Location interpretAndThenUpdateClientLocation(int user_id,
			Location locationRaw) throws LocationUpdateBadException,
			DataStoreFailureException {
		Location locationLast = null;
		// decide if the new location update contains any "as-previous"
		boolean needPrev = locationRaw.needPrev();
		if (needPrev) {
			// Complete the newest location update and store it in the database.
			locationLast = updateClientLocationNeedPrev(user_id, locationRaw);
		} else {
			// Store the newest location update in the database.
			locationLast = updateClientLocationIndep(user_id, locationRaw);
		}
		// Update AP-client store
		updateAPClientFromLocation(user_id, locationRaw);
		// update GPSGrid-client
		updateGPSGridClientFromLocation(user_id, locationRaw);
		return locationLast;
	}

	/**
	 * Complete the newest location update and store it in the database.
	 * 
	 * @param user_id
	 * @param locationRaw
	 * @return Last location.
	 * @throws LocationUpdateBadException
	 * @throws DataStoreFailureException
	 */
	protected Location updateClientLocationNeedPrev(int user_id,
			Location locationRaw) throws LocationUpdateBadException,
			DataStoreFailureException {
		Location locationLast = null;
		// get last location
		ExpirableStoreClient<Integer, Location, String, MinimumExpirableUnit<Location>, byte[]>.Updatable updatable = null;
		try {
			updatable = clientLocation.getUpdatable(Integer.valueOf(user_id));
		} catch (SerializationFailureException e) {
			throw new LocationUpdateBadException(
					LocationUpdateBadException.UPDATE_MORE_DETAIL);
		}
		if (updatable != null)
			locationLast = (Location) updatable.getValue();
		// Complete the location update, by populating "asPrev"
		// data with previous value.
		if (locationLast == null) {
			throw new LocationUpdateBadException(
					LocationUpdateBadException.UPDATE_MORE_DETAIL);
		}
		try {
			locationRaw.populateAsPrev(locationLast);
		} catch (NoPreviousDataException e) {
			throw new LocationUpdateBadException(
					LocationUpdateBadException.UPDATE_MORE_DETAIL);
		}
		// update data store
		updatable.setValue(locationRaw);
		try {
			updatable.commitValue();
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}
		try {
			clientLocation.putUpdatable(user_id, updatable);
		} catch (ObsoleteVersionException e) {
			// Someone has changed the clientLocation table during our process
			// of read-and-then-update. Break the tie by requesting an
			// independent location update.
			throw new LocationUpdateBadException(
					LocationUpdateBadException.UPDATE_MORE_DETAIL);
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}
		return locationLast;
	}

	/**
	 * Store the newest location update in the database.
	 * 
	 * @param user_id
	 * @param locationRaw
	 * @return Last location.
	 * @throws DataStoreFailureException
	 */
	protected Location updateClientLocationIndep(int user_id,
			Location locationRaw) throws DataStoreFailureException {
		Location locationLast = null;
		// get last location
		Integer user = Integer.valueOf(user_id);
		try {
			locationLast = clientLocation.get(user_id);
		} catch (SerializationFailureException e1) {
			throw new DataStoreFailureException(e1);
		}
		// update data store
		try {
			clientLocation.put(user, locationRaw);
		} catch (SerializationFailureException e) {
			throw new DataStoreFailureException(e);
		}
		return locationLast;
	}

	/**
	 * Update the topology log with new topology.
	 */
	protected void logTopology(int user_id, Map<Integer, Float> links) {
		UserLogManager user = new UserLogManager(user_id, "topoInfo",
				"/topo.log", "%d\t%m\n", false);
		JSONObject json = new JSONObject();
		try {
			json.put("topo", links);
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
			return;
		}
		user.info(json.toString());
		user.close();
	}

}
