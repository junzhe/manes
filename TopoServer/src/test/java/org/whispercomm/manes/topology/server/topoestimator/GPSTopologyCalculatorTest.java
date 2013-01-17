package org.whispercomm.manes.topology.server.topoestimator;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner.GuiceModules;
import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.linkestimator.GPSPairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.modules.TestModule;
import org.whispercomm.manes.topology.server.topoestimator.GPSTopologyCalculator;

import com.google.inject.Inject;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({TestModule.class})
public class GPSTopologyCalculatorTest {
	GPSTopologyCalculator gps;
	Location loc;
	
	@Inject
	protected void setUp(DataInterface Data){
		gps = new GPSTopologyCalculator(Data, new GPSPairwiseLinkEstimator());
		this.loc =Utility.initLocationInstance();
	}
	
	@Test
	public void testCalculate(){
		Map<Integer,Float> map = gps.calculate(0, loc);
		assertFalse(map.containsKey(0));
		assertTrue(map.containsKey(1));
		assertTrue(map.containsKey(2));
		assertTrue(map.containsKey(3));
	}
	
	@Test
	public void getGridNeighborsOfLatitudeTest(){
		Set<GpsGridElement> result = new HashSet<GpsGridElement>();
		int latIndex = 0;
		double longitude = 0;
		double lonStepSize = GpsGridElement.getLonStepSize(latIndex);
		int lonStepNum = GpsGridElement.getLonStepNum(latIndex);
		int lonIndex = (int) Math.floor(longitude / lonStepSize);
		gps.getGridNeighborsOfLatitude(latIndex, longitude, result);
		for (int i = -1; i <= 1; i++) {
			assertTrue(result.contains(new GpsGridElement(latIndex,(lonIndex+i)%lonStepNum)));
		}
	}
	
	@Test
	public void getCandidateGPSGridTest(){
		Set<GpsGridElement> result = null;
		GPS client = new GPS();
		client.setLat(0);
		client.setLon(0);
		GpsGridElement grid = new GpsGridElement(client);
		result = gps.getCandidateGPSGrid(client, grid);
		double lonStepSize = GpsGridElement.getLonStepSize(grid.getLatIndex());
		int lonStepNum = GpsGridElement.getLonStepNum(grid.getLatIndex());
		int lonIndex = (int) Math.floor(0 / lonStepSize);
		for(int j = -1; j<=1; j++){
			for (int i = -1; i <= 1; i++) {
				assertTrue(result.contains(new GpsGridElement(grid.getLatIndex()+j, (lonIndex + i) % lonStepNum)));
			}
		}
	}
	
	@Test
	public void getGridClientsTest() {
		Set<GpsGridElement> cand = new HashSet<GpsGridElement>();
		GpsGridElement grid = new GpsGridElement(0,0);
		cand.add(grid);
		Set<Integer> result = gps.getCandidNeighbors(cand, 0);
		assertTrue(result.contains(1));
		assertTrue(result.contains(2));
		assertTrue(result.contains(3));
		assertFalse(result.contains(0));
	}
}
