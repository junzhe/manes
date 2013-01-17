package org.whispercomm.manes.topology.server.topoestimator;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.linkestimator.WifiPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.topoestimator.WifiTopologyCalculator;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

public class WifiTopologyCalculatorTest {

	DataInterface dataStore;
	WifiPairwiseLinkEstimator linkEstimator;
	WifiTopologyCalculator topoUpdater;

	public void setUp() {
		dataStore = mock(DataInterface.class);
		linkEstimator = mock(WifiPairwiseLinkEstimator.class);
		topoUpdater = new WifiTopologyCalculator(dataStore, linkEstimator);
	}

	@Test
	public void testGetCandidNeighbors() throws DataStoreFailureException {
		setUp();
		int user_id = 0;
		WifiTopologyCalculator topoUpdaterSpy = spy(topoUpdater);
		// aps null
		List<Wifi> aps = null;
		Set<Integer> neighbors = topoUpdaterSpy.getCandidNeighbors(aps, user_id);
		assertTrue(neighbors.size() == 0);
		// aps no content
		aps = new LinkedList<Wifi>();
		neighbors = topoUpdaterSpy.getCandidNeighbors(aps, user_id);
		assertTrue(neighbors.size() == 0);
		// aps valid content
		int apNum = 5;
		for (int i = 0; i < apNum; i++) {
			Wifi wifi = new Wifi();
			wifi.setAp(i);
			aps.add(wifi);
		}
		neighbors = topoUpdaterSpy.getCandidNeighbors(aps, user_id);
		for (int i = 0; i < apNum; i++) {
			verify(dataStore).getAPClient(i);
		}
	}

	@Test
	public void testCalculateNull() throws DataStoreFailureException {
		setUp();
		WifiTopologyCalculator topoUpdaterSpy = spy(topoUpdater);
		int user_id = 0;
		// null location
		Location loc = null;
		Map<Integer, Float> links = topoUpdaterSpy.calculate(user_id, loc);
		assertTrue(links.size() == 0);
		// null wifi
		loc = mock(Location.class);
		when(loc.getWifi()).thenReturn(null);
		links = topoUpdaterSpy.calculate(user_id, loc);
		assertTrue(links.size() == 0);
		// null wifi
		Wifis wifis = mock(Wifis.class);
		when(loc.getWifi()).thenReturn(wifis);
		when(wifis.getWifi()).thenReturn(null);
		links = topoUpdaterSpy.calculate(user_id, loc);
		assertTrue(links.size() == 0);
	}

	@Test
	public void testCalculate() throws DataStoreFailureException {
		setUp();
		WifiTopologyCalculator topoUpdaterSpy = spy(topoUpdater);
		int user_id = 0;
		// valid wifi
		Location loc = mock(Location.class);
		Wifis wifis = mock(Wifis.class);
		when(loc.getWifi()).thenReturn(wifis);
		List<Wifi> aps = new LinkedList<Wifi>();
		when(wifis.getWifi()).thenReturn(aps);
		Set<Integer> neighbors = new HashSet<Integer>();
		int neighborNum = 5;
		
		for (int i = 0; i < neighborNum; i++) {
			neighbors.add(Integer.valueOf(i));
		}
		when(topoUpdaterSpy.getCandidNeighbors(aps, user_id)).thenReturn(neighbors);
		Location location = mock(Location.class);
		when(dataStore.getClientLocation(anyInt())).thenReturn(location);
		when(
				linkEstimator.getLinkQuality(any(Location.class),
						any(Location.class))).thenReturn((float) 0.5);
		Map<Integer, Float> links = topoUpdaterSpy.calculate(user_id, loc);
		System.out.println(links.size());
		assertTrue(links.size() == neighborNum);
	}

	@Test
	public void testCalculateUnEstimatable() throws DataStoreFailureException {
		setUp();
		WifiTopologyCalculator topoUpdaterSpy = spy(topoUpdater);
		int user_id = 0;
		// valid wifi
		Location loc = mock(Location.class);
		Wifis wifis = mock(Wifis.class);
		when(loc.getWifi()).thenReturn(wifis);
		List<Wifi> aps = new LinkedList<Wifi>();
		when(wifis.getWifi()).thenReturn(aps);
		Set<Integer> neighbors = new HashSet<Integer>();
		int neighborNum = 5;
		for (int i = 0; i < neighborNum; i++) {
			neighbors.add(Integer.valueOf(i));
		}
		when(topoUpdaterSpy.getCandidNeighbors(aps, user_id)).thenReturn(neighbors);
		when(
				linkEstimator.getLinkQuality(any(Location.class),
						any(Location.class))).thenReturn(
				PairwiseLinkEstimator.LINK_UNESTIMATABLE);
		Map<Integer, Float> links = topoUpdaterSpy.calculate(user_id, loc);
		assertTrue(links.size() == 0);
	}
}
