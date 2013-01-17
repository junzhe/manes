package org.whispercomm.manes.topology.server.topoestimator;

import org.whispercomm.manes.topology.server.data.DataInterface;

/**
 * Factory class that creates {@link TopologyEstimatorWifiGps}.
 * 
 * @author Yue Liu
 * 
 */
public class TopologyEstimatorWifiGpsFactory implements
		TopologyEstimatorFactory {

	@Override
	public TopologyEstimator create(DataInterface dataStore, int userId) {
		return new TopologyEstimatorWifiGps(dataStore, userId);
	}

}
