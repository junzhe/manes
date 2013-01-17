package org.whispercomm.manes.topology.server.manager;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;
import org.whispercomm.manes.topology.server.logger.UserLogManager;
import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimator;
import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimatorFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Default implementation of {@link TopologyManager} based on
 * {@link ExecutorService}.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
public class TopologyManagerDefault implements TopologyManager {

	private final ExecutorService topoExecutor;
	private final DataInterface data;
	private final TopologyEstimatorFactory topologyEstimatorFactory;
	private final Logger logger;

	@Inject
	public TopologyManagerDefault(
			@Named("topoExe") ExecutorService topoExecutor, DataInterface data,
			TopologyEstimatorFactory topologyEstimatorFactory) {
		this.topoExecutor = topoExecutor;
		this.data = data;
		this.topologyEstimatorFactory = topologyEstimatorFactory;
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@Override
	public Location preprocess(int user_id, Location location)
			throws LocationUpdateBadException, DataStoreFailureException {
		Location locationLast = data.interpretAndThenUpdateClientLocation(
				user_id, location);
		return locationLast;
	}

	@Override
	public void updateTraceLog(int user_id, Location locationLast,
			Location locationCrt) {
		UserLogManager user = new UserLogManager(user_id, "locInfo",
				"/location.log", "%d\t%m\n", false);
		user.info(locationCrt);
		user.close();
	}

	@Override
	public void updateTopology(int user_id, Location locationLast,
			Location locationCrt) {
		// update topology estimation
		TopologyEstimator topologyEstimator = topologyEstimatorFactory.create(
				data, user_id);
		topoExecutor.execute(new UpdateRunnable(data, user_id,
				topologyEstimator, locationLast, locationCrt, logger));
	}

	/**
	 * Wrapper class of {@link LocationUpdateHandler} into a {@link Runnable}.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public static class UpdateRunnable extends LocationUpdateHandler implements
			Runnable {

		private final Logger runnableLogger;

		public UpdateRunnable(DataInterface dataStore, int user_id,
				TopologyEstimator topoEstimator, Location locationLast,
				Location locationCrt, Logger logger) {
			super(user_id, locationLast, locationCrt, dataStore, topoEstimator);
			this.runnableLogger = logger;
		}

		@Override
		public void run() {
			try {
				handle();
			} catch (Throwable e) {
				runnableLogger.error(e.getMessage(), e);
			}
		}
	}

}
