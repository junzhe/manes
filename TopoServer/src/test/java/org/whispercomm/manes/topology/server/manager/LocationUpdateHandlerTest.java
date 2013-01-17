package org.whispercomm.manes.topology.server.manager;

import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.server.topoestimator.EstimationFailureException;
import org.whispercomm.manes.topology.server.topoestimator.TopologyEstimator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;

public class LocationUpdateHandlerTest {

	int user_id;
	Location locRaw;
	Location locComp;
	DataInterface dataInf;
	TopologyEstimator topoEstimator;
	LocationUpdateHandler handler;

	public void setUp() throws EstimationFailureException {
		user_id = 0;
		locRaw = mock(Location.class);
		locComp = mock(Location.class);
		dataInf = mock(DataInterface.class);
		topoEstimator = mock(TopologyEstimator.class);
		when(topoEstimator.estimate(locRaw, locComp)).thenReturn(null);
		handler = new LocationUpdateHandler(user_id, locRaw, locComp, dataInf,
				topoEstimator);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHandle() throws EstimationFailureException, DataStoreFailureException{
		setUp();
		handler.handle();
		verify(topoEstimator).estimate(locRaw, locComp);
		verify(dataInf).setClientNeighbors(anyInt(), anyMap());
	}

}
