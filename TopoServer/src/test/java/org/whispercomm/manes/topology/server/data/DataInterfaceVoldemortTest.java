package org.whispercomm.manes.topology.server.data;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.whispercomm.manes.server.expirablestore.serialization.SerializationFailureException;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.server.expirablestore.ExpirableStoreClient;
import org.whispercomm.manes.server.expirablestore.ExpirableStoreClient.Updatable;
import org.whispercomm.manes.topology.location.CDMA;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GSM;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataInterfaceVoldemortTest {

	ExpirableStoreClient clientLocation = mock(ExpirableStoreClient.class);
	ExpirableStoreClient APClient = mock(ExpirableStoreClient.class);
	ExpirableStoreClient GPSGridClient = mock(ExpirableStoreClient.class);
	ExpirableStoreClient topology = mock(ExpirableStoreClient.class);
	DataInterfaceVoldemort dataInterface = new DataInterfaceVoldemort(
			clientLocation, APClient, GPSGridClient, topology);

	@Test
	public void testGetClientLocation() throws DataStoreFailureException,
			SerializationFailureException {
		int user_id = 0;
		dataInterface.getClientLocation(user_id);
		verify(clientLocation).get(anyObject());
	}

	@Test
	public void testUpdateAPClientFromLocation()
			throws SerializationFailureException {
		int user_id = 0;
		// null location
		dataInterface.updateAPClientFromLocation(user_id, null);
		Set<Integer> clientSet = new HashSet<Integer>();
		clientSet.add(user_id);
		Location loc = mock(Location.class);
		Wifis wifis = mock(Wifis.class);
		// valid wifi information
		when(loc.getWifi()).thenReturn(null);
		dataInterface.updateAPClientFromLocation(user_id, loc);
		// valid wifi information
		when(loc.getWifi()).thenReturn(wifis);
		List<Wifi> wifiList = new LinkedList<Wifi>();
		Wifi wifi = new Wifi();
		Long ap = new Long(0);
		wifi.setAp(ap);
		int wifiNum = 5;
		for (int i = 0; i < wifiNum; i++) {
			wifiList.add(wifi);
		}
		when(wifis.getWifi()).thenReturn(wifiList);
		dataInterface.updateAPClientFromLocation(user_id, loc);
		verify(APClient, times(wifiNum)).update(ap, clientSet);
	}

	@Test
	public void testUpdateGPSGridClientFromLocation()
			throws SerializationFailureException {
		int user_id = 0;
		Location loc = mock(Location.class);
		// null GPS
		when(loc.getGps()).thenReturn(null);
		dataInterface.updateGPSGridClientFromLocation(user_id, loc);
		// valid GPS
		when(loc.getGps()).thenReturn(new GPS());
		dataInterface.updateGPSGridClientFromLocation(user_id, loc);
		verify(GPSGridClient).update(anyObject(), anyObject());
	}

	@Test
	public void testUpdateNeighborTopology()
			throws SerializationFailureException {
		Integer user = Integer.valueOf(0);
		// null neighbors
		Map<Integer, Float> neighbors = null;
		dataInterface.updateNeighborTopology(user, neighbors);
		// size-zero neighbors
		neighbors = new HashMap<Integer, Float>();
		dataInterface.updateNeighborTopology(user, neighbors);
		// valid neighbors
		int neighborNum = 5;
		for (int i = 0; i < neighborNum; i++) {
			neighbors.put(Integer.valueOf(i), Float.valueOf((float) 0.5));
		}
		dataInterface.updateNeighborTopology(user, neighbors);
		verify(topology, times(neighborNum)).update(anyObject(), anyObject());
	}

	@Test
	public void testSetClientNeighbors() throws DataStoreFailureException,
			SerializationFailureException {
		int user_id = 0;
		HashMap<Integer, Float> neighbors = new HashMap<Integer, Float>();
		DataInterfaceVoldemort dataInfSpy = spy(dataInterface);
		dataInfSpy.setClientNeighbors(user_id, neighbors);
		verify(topology).put(user_id, neighbors);
		verify(dataInfSpy).updateNeighborTopology(user_id, neighbors);
	}

	@Test
	public void testInterpretAndThenUpdateClientLocationNew()
			throws SerializationFailureException, LocationUpdateBadException,
			DataStoreFailureException {
		// do not need previous record to complete the information
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

		int user_id = 0;
		dataInterface.interpretAndThenUpdateClientLocation(user_id, loc);
		verify(clientLocation).get(anyObject());
		verify(clientLocation).put(anyObject(), anyObject());
	}

	@Test
	public void testInterpretAndThenUpdateClientLocationAsPrevUpdatableNull()
			throws SerializationFailureException, DataStoreFailureException {
		// do need previous record to complete the information
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(true);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);

		// updatable null
		int user_id = 0;
		Integer user = Integer.valueOf(user_id);
		when(clientLocation.getUpdatable(anyObject())).thenReturn(null);
		try {
			dataInterface.interpretAndThenUpdateClientLocation(user_id, loc);
			fail();
		} catch (LocationUpdateBadException e) {
			verify(clientLocation).getUpdatable(user);
		}
	}

	@Test
	public void testInterpretAndThenUpdateClientLocationAsPrev()
			throws SerializationFailureException, LocationUpdateBadException,
			DataStoreFailureException {
		// do need previous record to complete the information
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(true);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);

		Updatable updatable = mock(Updatable.class);
		when(clientLocation.getUpdatable(anyObject())).thenReturn(updatable);
		Location locPrev = mock(Location.class);
		GPS gpsPrev = mock(GPS.class);
		when(locPrev.getGps()).thenReturn(gpsPrev);
		GSM gsmPrev = mock(GSM.class);
		when(locPrev.getGsm()).thenReturn(gsmPrev);
		CDMA cdmaPrev = mock(CDMA.class);
		when(locPrev.getCdma()).thenReturn(cdmaPrev);
		Wifis wifisPrev = mock(Wifis.class);
		when(locPrev.getWifi()).thenReturn(wifisPrev);
		when(updatable.getValue()).thenReturn(locPrev);

		int user_id = 0;
		Integer user = Integer.valueOf(user_id);
		dataInterface.interpretAndThenUpdateClientLocation(user_id, loc);
		verify(clientLocation).getUpdatable(user);
		verify(clientLocation).putUpdatable(user, updatable);
	}

	@Test
	public void testInterpretAndThenUpdateClientLocationAsPrevContentNull()
			throws SerializationFailureException, DataStoreFailureException {
		// do not need previous record to complete the information
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(true);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(false);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);

		Updatable updatable = mock(Updatable.class);
		when(clientLocation.getUpdatable(anyObject())).thenReturn(null);
		Location locPrev = mock(Location.class);
		when(updatable.getValue()).thenReturn(locPrev);

		int user_id = 0;
		Integer user = Integer.valueOf(user_id);
		try {
			dataInterface.interpretAndThenUpdateClientLocation(user_id, loc);
			fail();
		} catch (LocationUpdateBadException e) {
			verify(clientLocation).getUpdatable(user);
		}
	}

	@Test
	public void testInterpretAndThenUpdateClientLocationContentNull()
			throws SerializationFailureException, DataStoreFailureException,
			LocationUpdateBadException {
		// do not need previous record to complete the information
		Location loc = mock(Location.class);
		when(loc.getGps()).thenReturn(null);
		when(loc.getGsm()).thenReturn(null);
		when(loc.getCdma()).thenReturn(null);
		when(loc.getWifi()).thenReturn(null);

		Updatable updatable = mock(Updatable.class);
		when(clientLocation.getUpdatable(anyObject())).thenReturn(updatable);

		int user_id = 0;
		Integer user = Integer.valueOf(user_id);
		dataInterface.interpretAndThenUpdateClientLocation(user_id, loc);
		verify(clientLocation).get(user);
		verify(clientLocation).put(user, loc);
	}
}
