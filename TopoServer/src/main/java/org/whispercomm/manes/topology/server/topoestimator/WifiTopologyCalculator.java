package org.whispercomm.manes.topology.server.topoestimator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.WifiPairwiseLinkEstimator;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;

import com.google.inject.Inject;

/**
 * Update topology estimation based on WiFi measurements.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
public class WifiTopologyCalculator implements TopologyCalculator {

	private final DataInterface dataStore;
	private final WifiPairwiseLinkEstimator linkEstimator;
	private final Logger log;

	@Inject
	public WifiTopologyCalculator(DataInterface dataStore, WifiPairwiseLinkEstimator linkEstimator) {
		this.dataStore = dataStore;
		this.linkEstimator = linkEstimator;
		this.log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public Map<Integer, Float> calculate(int user_id, Location location) {
		HashMap<Integer, Float> links = new HashMap<Integer, Float>();
		if (location == null)
			return links;
		Wifis wifis = location.getWifi();
		if (wifis == null)
			return links;
		List<Wifi> aps = wifis.getWifi();
		if (aps == null)
			return links;

		// Find all possible neighbors through the given user's APs.
		Set<Integer> neighbors = getCandidNeighbors(aps, user_id);

		// Iterate through all the potential neighbors and estimate their link
		// quality.
		Iterator<Integer> nIt = neighbors.iterator();
		int neighbor_id;
		float link_quality;
		while (nIt.hasNext()) {
			neighbor_id = nIt.next();
			try {
				link_quality = linkEstimator.getLinkQuality(location,
						dataStore.getClientLocation(neighbor_id));
			} catch (DataStoreFailureException e) {
				log.error(e.getMessage(), e);
				continue;
			}
			// not necessary to report neighbors with 0 link quality.
			if (link_quality != PairwiseLinkEstimator.LINK_UNESTIMATABLE) {
				links.put(new Integer(neighbor_id), new Float(link_quality));
			}
		}
		return links;
	}

	// Find all possible neighbors through the given user's APs.
	protected Set<Integer> getCandidNeighbors(List<Wifi> aps, int user_id) {

		Set<Integer> neighbors = new HashSet<Integer>();

		if (aps == null)
			return neighbors;

		Iterator<Wifi> it = aps.iterator();
		Wifi APCrt;
		Set<Integer> clientsCrt;
		while (it.hasNext()) {
			APCrt = it.next();
			try {
				clientsCrt = dataStore.getAPClient(APCrt.getAp());
			} catch (DataStoreFailureException e) {
				log.error(e.getMessage(), e);
				continue;
			}
			if (clientsCrt != null) {
				neighbors.addAll(clientsCrt);
			}
		}

		Integer user = Integer.valueOf(user_id);
		if (neighbors.contains(user))
			neighbors.remove(user);
		return neighbors;
	}
}
