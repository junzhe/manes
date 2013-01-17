package org.whispercomm.manes.topology.location;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.testutility.Utility;

public class GPSGridElementTest {

	GpsGridElement grid;
	
	@Before
	public void setUp(){
		grid = new GpsGridElement(Utility.initGPSInstance(false));
	}
	
	@Test
	public void testLonStepSize(){
		assertTrue(GpsGridElement.getLonStepSize(0)==0.002247668980396463);
		assertTrue(GpsGridElement.getLonStepSize(10)==0.002247669153346964);
		assertTrue(GpsGridElement.getLonStepSize(20)==0.0022476696721985983);
		assertTrue(GpsGridElement.getLonStepSize(30)==0.0022476705369517676);
	}
	
	@Test
	public void testLongStepNum(){
		assertTrue(GpsGridElement.getLonStepNum(0)==(int)Math.ceil(360/GpsGridElement.getLonStepSize(0)));
		assertTrue(GpsGridElement.getLonStepNum(10)==(int)Math.ceil(360/GpsGridElement.getLonStepSize(10)));
		assertTrue(GpsGridElement.getLonStepNum(20)==(int)Math.ceil(360/GpsGridElement.getLonStepSize(20)));
		assertTrue(GpsGridElement.getLonStepNum(30)==(int)Math.ceil(360/GpsGridElement.getLonStepSize(30)));
	}
	
	@Test
	public void testConstructor(){
		assertTrue(grid.getLatIndex()==24714);
		assertTrue(grid.getLonIndex()==16777);
	}
	
	@Test
	public void testSet(){
		grid.setLatIndex(1000);
		assertTrue(grid.getLatIndex()==1000);
		grid.setLonIndex(1000);
		assertTrue(grid.getLonIndex()==1000);
	}
}
