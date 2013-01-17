package org.whispercomm.manes.topology.location;

import static org.junit.Assert.*;

import org.junit.Test;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.testutility.Utility;

public class GPSTest {
	
	@Test
	public void getDistanceTest(){
		GPS gps = new GPS();
		assertTrue(GPS.getDistance(Utility.initGPSInstance(false), Utility.initGPSInstance(false))==0.0);
		gps.setLat(100);
		gps.setLon(100);
		//assertTrue(GPS.getDistance(Utility.initGPSInstance(false), gps)==4960330.552904812);
		System.out.println(GPS.getDistance(Utility.initGPSInstance(false), gps));
		gps.setLat(0);
		gps.setLon(0);
		//assertTrue(GPS.getDistance(Utility.initGPSInstance(false), gps)==6158461.033783251);
		System.out.println(GPS.getDistance(Utility.initGPSInstance(false), gps));
	}
}
