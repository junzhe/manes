package org.whispercomm.manes.topology.server.modules;

import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.data.DataInterface4Test;
import org.whispercomm.manes.topology.server.linkestimator.GPSPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.WifiPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.manager.TopologyManager;
import org.whispercomm.manes.topology.server.manager.TopologyManager4Test;
import org.whispercomm.manes.topology.server.topoestimator.GPSTopologyCalculator;
import org.whispercomm.manes.topology.server.topoestimator.TopologyCalculator;
import org.whispercomm.manes.topology.server.topoestimator.WifiTopologyCalculator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TopologyCalculator.class).annotatedWith(Names.named("WifiUpdater"))
				.to(WifiTopologyCalculator.class);
		bind(TopologyCalculator.class).annotatedWith(Names.named("GPSUpdater"))
				.to(GPSTopologyCalculator.class);
		bind(DataInterface.class).to(DataInterface4Test.class);
		bind(PairwiseLinkEstimator.class).annotatedWith(Names.named("Wifi"))
				.to(WifiPairwiseLinkEstimator.class);
		bind(PairwiseLinkEstimator.class).annotatedWith(Names.named("GPS")).to(
				GPSPairwiseLinkEstimator.class);
		bind(PairwiseLinkEstimator.class).annotatedWith(Names.named("Test"))
				.to(GPSPairwiseLinkEstimator.class);
		bind(TopologyManager.class).annotatedWith(Names.named("Data")).to(
				TopologyManager4Test.class);
	}
}
