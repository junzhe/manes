package org.whispercomm.manes.topology.server.modules;

import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimatorFactory;
import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimatorWifiGpsFactory;

import com.google.inject.AbstractModule;

public class TopologyManagerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TopologyEstimatorFactory.class).to(
				TopologyEstimatorWifiGpsFactory.class);
	}

}
