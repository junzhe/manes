package org.whispercomm.manes.topology.server.topoestimator;

import java.util.Iterator;
import java.util.Map;

import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.linkestimator.GPSPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.WifiPairwiseLinkEstimator;

/**
 * This estimator always prefers Wifi-based estimation over GPS-based
 * estimation.
 * 
 * @author Yue Liu
 * 
 */
public class TopologyEstimatorWifiGps implements TopologyEstimator {

	private final int userId;
	private final DataInterface dataStore;
	private final GPSTopologyCalculator gpsTopoCalculator;
	private final WifiTopologyCalculator wifiTopoCalculator;

	public TopologyEstimatorWifiGps(DataInterface dataStore, int userId) {
		this.userId = userId;
		this.dataStore = dataStore;
		this.gpsTopoCalculator = new GPSTopologyCalculator(dataStore,
				new GPSPairwiseLinkEstimator());
		this.wifiTopoCalculator = new WifiTopologyCalculator(dataStore,
				new WifiPairwiseLinkEstimator());
	}

	public TopologyEstimatorWifiGps(DataInterface dataStore, int userId,
			GPSTopologyCalculator gpsTopoCalculator,
			WifiTopologyCalculator wifiTopoCalculator) {
		this.userId = userId;
		this.dataStore = dataStore;
		this.gpsTopoCalculator = gpsTopoCalculator;
		this.wifiTopoCalculator = wifiTopoCalculator;
	}

	/**
	 * The new topology is as previous if both Wifi and GPS fields are exactly
	 * the same as previous.
	 */
	@Override
	public boolean asPrevious(Location locationLast, Location locationCrt) {
		if (locationLast == null || locationCrt == null)
			return false;
		boolean asPrev = true;
		Wifis wifi = locationCrt.getWifi();
		if (wifi != null) {
			asPrev = asPrev && wifi.getAsPrev();
		} else {
			asPrev = asPrev && (locationLast.getWifi() == null);
		}
		GPS gps = locationCrt.getGps();
		if (gps != null) {
			asPrev = asPrev && gps.getAsPrev();
		} else {
			asPrev = asPrev && (locationLast.getGps() == null);
		}
		return asPrev;
	}

	@Override
	public Map<Integer, Float> estimate(Location locationLast,
			Location locationCrt) throws EstimationFailureException {

		Map<Integer, Float> links = null;
		// topology as previous
		if (asPrevious(locationLast, locationCrt)) {
			try {
				links = dataStore.getClientNeighbors(userId);
			} catch (DataStoreFailureException e) {
				throw new EstimationFailureException(e);
			}
			return links;
		}
		// Re-calculate topology
		Map<Integer, Float> linksWifi = wifiTopoCalculator.calculate(userId,
				locationCrt);
		Map<Integer, Float> linksGps = gpsTopoCalculator.calculate(userId,
				locationCrt);
		return mergeTopology(linksWifi, linksGps);
	}

	/**
	 * Merge topology estimations from WifiTopologyCalculater and
	 * GpsTopologyCalculator.
	 * 
	 * @param linksWifi
	 *            WifiTopologyCalculater based topology estimation.
	 * @param linksGps
	 *            GpsTopologyCalculator based topology estimation.
	 * @return
	 */
	protected Map<Integer, Float> mergeTopology(Map<Integer, Float> linksWifi,
			Map<Integer, Float> linksGps) {
		if (linksWifi.size() == 0)
			return linksGps;
		Iterator<Integer> it = linksWifi.keySet().iterator();
		Integer neighborCrt;
		while (it.hasNext()) {
			neighborCrt = it.next();
			linksGps.remove(neighborCrt);
		}
		linksWifi.putAll(linksGps);
		return linksWifi;
	}

}
