package org.whispercomm.manes.topology.server.topoestimator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.CDMA;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GSM;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.data.DataInterface;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyMap;

public class TopologyEstimatorWifiGpsTest {

	TopologyEstimatorWifiGps topoEstimator;
	DataInterface dataInf;
	int user_id;
	GPSTopologyCalculator gpsTopoCalculator;
	WifiTopologyCalculator wifiTopoCalculator;

	public void setUp() {
		dataInf = mock(DataInterface.class);
		user_id = 0;
		gpsTopoCalculator = mock(GPSTopologyCalculator.class);
		wifiTopoCalculator = mock(WifiTopologyCalculator.class);
		topoEstimator = new TopologyEstimatorWifiGps(dataInf, user_id,
				gpsTopoCalculator, wifiTopoCalculator);
	}

	@Test
	public void testAsPrevious() {
		setUp();
		// location
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(true);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(true);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// last location
		Location locPrev = mock(Location.class);
		assertTrue(topoEstimator.asPrevious(locPrev, loc));
	}

	@Test
	public void testAsPreviousFalse() {
		setUp();
		// location
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(true);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// last location
		Location locPrev = mock(Location.class);
		assertFalse(topoEstimator.asPrevious(locPrev, loc));
	}

	@Test
	public void testAsPreviousNull() {
		setUp();
		// location
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(true);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		// last location
		Location locPrev = mock(Location.class);
		when(locPrev.getWifi()).thenReturn(null);
		assertTrue(topoEstimator.asPrevious(locPrev, loc));
	}

	@Test
	public void testAsPreviousNullFalse() {
		setUp();
		// location
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// last location
		Location locPrev = mock(Location.class);
		when(locPrev.getGps()).thenReturn(new GPS());
		assertFalse(topoEstimator.asPrevious(locPrev, loc));
	}

	@Test
	public void testMergeToplogyOverlap() {
		setUp();
		int wifiNum = 3;
		float wifiLink = (float) 0.5;
		Map<Integer, Float> linksWifi = new HashMap<Integer, Float>();
		for (int i = 0; i < wifiNum; i++) {
			linksWifi.put(i, wifiLink);
		}
		int gpsNum = 3;
		float gpsLink = (float) 0.6;
		Map<Integer, Float> linksGps = new HashMap<Integer, Float>();
		for (int i = 0; i < (wifiNum + gpsNum); i++) {
			linksGps.put(i, gpsLink);
		}
		Map<Integer, Float> links = topoEstimator.mergeTopology(linksWifi,
				linksGps);
		assertTrue(wifiLink != gpsLink);
		assertTrue(links.size() == (wifiNum + gpsNum));
		for (int i = 0; i < wifiNum; i++) {
			assertTrue(links.get(i) == wifiLink);
		}
	}

	@Test
	public void testMergeToplogyNonOverlap() {
		setUp();
		int wifiNum = 3;
		float wifiLink = (float) 0.5;
		Map<Integer, Float> linksWifi = new HashMap<Integer, Float>();
		for (int i = 0; i < wifiNum; i++) {
			linksWifi.put(i, wifiLink);
		}
		int gpsNum = 3;
		float gpsLink = (float) 0.6;
		Map<Integer, Float> linksGps = new HashMap<Integer, Float>();
		for (int i = wifiNum; i < (2 * wifiNum + gpsNum); i++) {
			linksGps.put(i, gpsLink);
		}
		Map<Integer, Float> links = topoEstimator.mergeTopology(linksWifi,
				linksGps);
		assertTrue(wifiLink != gpsLink);
		assertTrue(links.size() == (2 * wifiNum + gpsNum));
	}

	@Test
	public void testEstimateAsPrev() throws EstimationFailureException,
			DataStoreFailureException {
		setUp();
		// location
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(true);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(true);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// last location
		Location locPrev = mock(Location.class);
		topoEstimator.estimate(locPrev, loc);
		verify(dataInf).getClientNeighbors(user_id);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEstimateNew() throws EstimationFailureException,
			DataStoreFailureException {
		setUp();
		// location
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(false);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// last location
		Location locPrev = mock(Location.class);
		TopologyEstimatorWifiGps topoEstimatorSpy = spy(topoEstimator);
		topoEstimatorSpy.estimate(locPrev, loc);
		verify(gpsTopoCalculator).calculate(user_id, loc);
		verify(wifiTopoCalculator).calculate(user_id, loc);
		verify(topoEstimatorSpy).mergeTopology(anyMap(), anyMap());
	}
}
