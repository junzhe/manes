package org.whispercomm.manes.topology.location;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class LocationTest {

	@Test
	public void testNeedPrev() {
		// need previous
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
		assertTrue(loc.needPrev());
	}

	@Test
	public void testNeedPreviousFalse() {
		// do not need previous
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
		assertFalse(loc.needPrev());
	}

	@Test
	public void testPopulateAsPrev() {
		// need previous
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(true);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(true);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// previous location
		Location locPrev = mock(Location.class);
		GPS gpsPrev = mock(GPS.class);
		when(locPrev.getGps()).thenReturn(gpsPrev);
		GSM gsmPrev = mock(GSM.class);
		when(locPrev.getGsm()).thenReturn(gsmPrev);
		CDMA cdmaPrev = mock(CDMA.class);
		when(locPrev.getCdma()).thenReturn(cdmaPrev);
		Wifis wifisPrev = mock(Wifis.class);
		when(locPrev.getWifi()).thenReturn(wifisPrev);

		try {
			loc.populateAsPrev(locPrev);
		} catch (NoPreviousDataException e) {
			fail();
		}

		assertTrue(loc.getGps() != gpsPrev);
		assertTrue(loc.getGsm() == gsmPrev);
		assertTrue(loc.getCdma() != cdmaPrev);
		assertTrue(loc.getWifi() == wifisPrev);
	}

	@Test
	public void testPopulateAsPrevNoContent() {
		// need previous
		GPS gps = mock(GPS.class);
		when(gps.getAsPrev()).thenReturn(false);
		GSM gsm = mock(GSM.class);
		when(gsm.getAsPrev()).thenReturn(true);
		CDMA cdma = mock(CDMA.class);
		when(cdma.getAsPrev()).thenReturn(false);
		Wifis wifis = mock(Wifis.class);
		when(wifis.getAsPrev()).thenReturn(true);
		Location loc = new Location();
		loc.setGps(gps);
		loc.setGsm(gsm);
		loc.setCdma(cdma);
		loc.setWifi(wifis);
		// previous location
		Location locPrev = mock(Location.class);

		try {
			loc.populateAsPrev(locPrev);
			fail();
		} catch (NoPreviousDataException e) {
		}
	}
}
