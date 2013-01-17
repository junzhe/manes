package org.whispercomm.manes.topology.server.topoestimator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.linkestimator.GPSPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;

import com.google.inject.Inject;

/**
 * Update topology estimation based on WiFi measurements.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
public class GPSTopologyCalculator implements TopologyCalculator {
	private final DataInterface dataStore;
	private final GPSPairwiseLinkEstimator linkEstimator;
	private final Logger log;

	@Override
	public Map<Integer, Float> calculate(int user_id, Location location) {
		log.trace("Starting GPSTopologyUpdater");
		HashMap<Integer, Float> links = new HashMap<Integer, Float>();
		if (location == null)
			return links;
		GPS gps = location.getGps();
		if (gps == null)
			return links;
		GpsGridElement grid = new GpsGridElement(gps);
		log.trace("My GPS grid, latIndex=" + grid.getLatIndex()
				+ ", lonIndex=" + grid.getLonIndex());
		// get the candidate set of GPSGrids where neighbors may reside on.
		Set<GpsGridElement> gpsGrids = getCandidateGPSGrid(gps, grid);
		// Debug
		log.trace("My neighbor grids:");
		Iterator<GpsGridElement> itDebug = gpsGrids.iterator();
		while (itDebug.hasNext()) {
			GpsGridElement gridDebug = itDebug.next();
			log.trace("latIndex=" + gridDebug.getLatIndex() + ", lonIndex="
					+ gridDebug.getLonIndex());
		}
		// get the candidate set of neighbors
		Set<Integer> neighborCandidates = getCandidNeighbors(gpsGrids, user_id);
		log.trace("Number of candidate GPS neighbors: "
				+ neighborCandidates.size());

		Iterator<Integer> it = neighborCandidates.iterator();
		int user_tmp;
		Location locationtmp;
		while (it.hasNext()) {
			user_tmp = it.next();
			log.trace("Calculating link quality with user: " + user_tmp);
			try {
				locationtmp = dataStore.getClientLocation(user_tmp);
			} catch (DataStoreFailureException e) {
				log.error(e.getMessage(), e);
				continue;
			}
			float quality = linkEstimator.getLinkQuality(location, locationtmp);
			log.trace("Link quality with user " + user_tmp + ": " + quality);
			if (quality != PairwiseLinkEstimator.LINK_UNESTIMATABLE) {
				links.put(user_tmp, quality);
			}
		}
		return links;
	}

	/**
	 * Get a set of GPSGrids where candidate neighbors may reside, i.e., the
	 * neighboring 9 grids of the given client's GPSGrid.
	 * 
	 * @param client
	 *            the client's precise GPS location
	 * @param grid
	 *            the client's GPS grid
	 * @return
	 */
	protected Set<GpsGridElement> getCandidateGPSGrid(GPS client,
			GpsGridElement grid) {
		Set<GpsGridElement> candidGrids = new HashSet<GpsGridElement>();
		int latIndex = grid.getLatIndex();
		double longitude = client.getLon();
		// include the neighboring two grids, and the given GPS grid itself.
		getGridNeighborsOfLatitude(latIndex, longitude, candidGrids);
		// include the neighboring three grids on top of the given grid, i.e.,
		// on the latitude circle one step above.
		if (latIndex < GpsGridElement.LAT_INDEX_MAX) {
			getGridNeighborsOfLatitude(latIndex + 1, longitude, candidGrids);
		}
		// include the neighboring three grids below the given grid, i.e.,
		// on the latitude circle one step below.
		if (latIndex > GpsGridElement.LAT_INDEX_MIN) {
			getGridNeighborsOfLatitude(latIndex - 1, longitude, candidGrids);
		}
		return candidGrids;
	}

	/**
	 * Get the neighboring three GPS grid of a given longitude, on a given
	 * latitude circle.
	 * 
	 * @param latIndex
	 * @param longitude
	 * @param candidGrids
	 */
	protected void getGridNeighborsOfLatitude(int latIndex, double longitude,
			Set<GpsGridElement> candidGrids) {
		double lonStepSize = GpsGridElement.getLonStepSize(latIndex);
		int lonStepNum = GpsGridElement.getLonStepNum(latIndex);
		int lonIndex = (int) Math.floor(longitude / lonStepSize);
		for (int i = -1; i <= 1; i++) {
			candidGrids.add(new GpsGridElement(latIndex, (lonIndex + i)
					% lonStepNum));
		}
	}

	/**
	 * Get all the clients which reside on the given set of GPSGrids.
	 * 
	 * @param candidateGrids
	 * @param user_id
	 *            the given client's user_id
	 * @return
	 */
	protected Set<Integer> getCandidNeighbors(
			Set<GpsGridElement> candidateGrids, int user_id) {
		Set<Integer> clients = new HashSet<Integer>();
		if (candidateGrids.size() == 0)
			return clients;
		Iterator<GpsGridElement> it = candidateGrids.iterator();
		GpsGridElement gridCrt;
		Set<Integer> clientsCrt;
		while (it.hasNext()) {
			gridCrt = it.next();
			try {
				clientsCrt = dataStore.getGPSGridClient(gridCrt);
				// Debug
				// log.info("***Read gps grid, latIndex="
				// + gridCrt.getLatIndex() + ", lonIndex="
				// + gridCrt.getLonIndex());
				// log.info("***Client set:");
				// if (clientsCrt != null) {
				// Iterator<Integer> itDebug = clientsCrt.iterator();
				// while (itDebug.hasNext()) {
				// log.info("***client: " + itDebug.next());
				// }
				// }
			} catch (DataStoreFailureException e) {
				log.error(e.getMessage(), e);
				continue;
			}
			if (clientsCrt != null) {
				clients.addAll(clientsCrt);
			}
		}
		// Remove the client itself
		Integer user = Integer.valueOf(user_id);
		if (clients.contains(user))
			clients.remove(user);
		return clients;
	}

	@Inject
	public GPSTopologyCalculator(DataInterface dataStore, GPSPairwiseLinkEstimator linkEstimator) {
		this.dataStore = dataStore;
		this.linkEstimator = linkEstimator;
		this.log = LoggerFactory.getLogger(getClass());
	}

}
