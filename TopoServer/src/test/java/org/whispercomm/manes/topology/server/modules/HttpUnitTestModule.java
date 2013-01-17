package org.whispercomm.manes.topology.server.modules;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.whispercomm.manes.topology.server.TopoServer;
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
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class HttpUnitTestModule extends AbstractModule{
	static final int EXECUTOR_CORE_POOL_SIZE_DEFAULT = 16;
	static final int EXECUTOR_MAX_POOL_SIZE_DEFAULT = 128;
	static final long EXECUTOR_KEEP_ALIVE_TIME = 1000;
	static final TimeUnit EXECUTOR_TIME_UNIT = TimeUnit.MILLISECONDS;
	static final int EXECUTOR_QUEUE_CAPACITY = 10;
	static final String BOOT_STRAP_URL = "tcp://localhost:6666";

	@Override
	protected void configure() {
		bind(TopoServer.class).in(Singleton.class);
		bind(TopologyManager.class).to(TopologyManager4Test.class).in(
				Singleton.class);
		bind(TopologyCalculator.class).annotatedWith(Names.named("WifiUpdater"))
				.to(WifiTopologyCalculator.class);
		bind(TopologyCalculator.class).annotatedWith(Names.named("GPSUpdater"))
				.to(GPSTopologyCalculator.class);
		bind(DataInterface.class).to(DataInterface4Test.class);
		bind(PairwiseLinkEstimator.class).annotatedWith(Names.named("Wifi"))
				.to(WifiPairwiseLinkEstimator.class);
		bind(PairwiseLinkEstimator.class).annotatedWith(Names.named("GPS")).to(
				GPSPairwiseLinkEstimator.class);
		bind(ObjectMapper.class).in(Singleton.class);
	}

	/**
	 * Constructor of {@link ThreadPoolERxecutor} with default settings.
	 * 
	 * @return
	 */
	@Provides
	ThreadPoolExecutor provideThreadPoolExecutor() {
		return new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE_DEFAULT,
				EXECUTOR_MAX_POOL_SIZE_DEFAULT, EXECUTOR_KEEP_ALIVE_TIME,
				EXECUTOR_TIME_UNIT, new ArrayBlockingQueue<Runnable>(
						EXECUTOR_QUEUE_CAPACITY));
	}
}
