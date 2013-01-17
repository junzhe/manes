package org.whispercomm.manes.topology.server.manager;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.topoestimator.EstimationFailureException;
import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimator;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;

/**
 * Handle each {@link Location} update from clients. Specifically, it will
 * invoke a re-calculation of topology according to the newly updated
 * information.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
public class LocationUpdateHandler {

	private final DataInterface dataStore;
	private final Logger log;
	private final TopologyEstimator topoEstimator;
	private int user_id;
	private Location locationLast;
	private Location locationCrt;

	public LocationUpdateHandler(int user_id, Location locationLast,
			Location locationCrt, DataInterface dataStore,
			TopologyEstimator topoEstimator) {
		this.dataStore = dataStore;
		this.user_id = user_id;
		this.locationLast = locationLast;
		this.locationCrt = locationCrt;
		this.log = LoggerFactory.getLogger(getClass());
		this.topoEstimator = topoEstimator;
	}

	/**
	 * Make topology estimation and update corresponding database store.
	 */
	public void handle() {
		try {
			// make topology estimation.
			Map<Integer, Float> links = topoEstimator.estimate(locationLast,
					locationCrt);
			// push out the new estimation "links"
			dataStore.setClientNeighbors(user_id, links);
		} catch (EstimationFailureException e) {
			log.error(e.getMessage(), e);
		} catch (DataStoreFailureException e) {
			log.error(e.getMessage(), e);
		}
	}

}
