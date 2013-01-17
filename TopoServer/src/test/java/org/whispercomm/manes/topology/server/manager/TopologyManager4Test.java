package org.whispercomm.manes.topology.server.manager;

import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;

public class TopologyManager4Test implements TopologyManager {
	public boolean preprocessCalled = false;
	public boolean updateTraceLogCalled = false;
	public boolean updateTopologyCalled = false;
	@Override
	public Location preprocess(int user_id, Location location)
			throws LocationUpdateBadException, DataStoreFailureException {
		preprocessCalled = true;
		return Utility.initLocationInstance();
	}

	@Override
	public void updateTraceLog(int user_id, Location locationRaw,
			Location locationComp) {
		updateTraceLogCalled = true;
	}

	@Override
	public void updateTopology(int user_id, Location locationRaw,
			Location locationComp) {
		updateTopologyCalled = true;
	}
}
