package org.whispercomm.manes.topology.server.topoestimator;

import org.whispercomm.manes.topology.server.data.DataInterface;

/**
 * Factory to assist Guice injection of {@link TopologyEstimatorWifiGps} with
 * parameters.
 * 
 * @author Yue Liu
 * 
 */
public interface TopologyEstimatorFactory {
	public TopologyEstimator create(DataInterface dataStore, int userId);
}