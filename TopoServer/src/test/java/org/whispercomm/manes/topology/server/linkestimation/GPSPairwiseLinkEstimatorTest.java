package org.whispercomm.manes.topology.server.linkestimation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner.GuiceModules;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;
import org.whispercomm.manes.topology.server.modules.TestModule;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({TestModule.class})
public class GPSPairwiseLinkEstimatorTest {
	PairwiseLinkEstimator linkEstimator;
	
	@Inject
	protected void setUp(@Named("GPS") PairwiseLinkEstimator linkEstimator){
		this.linkEstimator = linkEstimator;
	}
	
	@Test
	public void testLinkUnestimatable(){
		Location loc = new Location();
		assertTrue(linkEstimator.getLinkQuality(Utility.initLocationInstance(),loc)==PairwiseLinkEstimator.LINK_UNESTIMATABLE);
		Location loc1 = new Location();
		assertTrue(linkEstimator.getLinkQuality(loc,Utility.initLocationInstance())==PairwiseLinkEstimator.LINK_UNESTIMATABLE);
		assertTrue(linkEstimator.getLinkQuality(loc,loc1)==PairwiseLinkEstimator.LINK_UNESTIMATABLE);
	}
	
	@Test
	public void testLinkEstimatable(){
		Location loc = new Location();
		GPS gps = new GPS();
		gps.setLat(1000);
		gps.setLon(1000);
		loc.setGps(gps);
		assertTrue(linkEstimator.getLinkQuality(Utility.initLocationInstance(),Utility.initLocationInstance())==1.0f);
		assertTrue(linkEstimator.getLinkQuality(Utility.initLocationInstance(), loc)==0.0f);
	}
}
