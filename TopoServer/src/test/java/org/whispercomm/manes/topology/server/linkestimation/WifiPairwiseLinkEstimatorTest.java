package org.whispercomm.manes.topology.server.linkestimation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifis;
import org.whispercomm.manes.topology.server.linkestimator.WifiPairwiseLinkEstimator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WifiPairwiseLinkEstimatorTest {

	WifiPairwiseLinkEstimator estimator;
	
	public void setUp(){
		estimator = new WifiPairwiseLinkEstimator();
	}
	
	@Test
	public void testGetLinkQualityUnestimatable(){
		setUp();
		
		Location client1 = null;
		Location client2 = null;
		float quality = estimator.getLinkQuality(client1, client2);
		assertTrue(quality ==  	WifiPairwiseLinkEstimator.LINK_UNESTIMATABLE);
		
		client1 = mock(Location.class);
		when(client1.getWifi()).thenReturn(null);
		client2 = mock(Location.class);
		when(client2.getWifi()).thenReturn(null);
		quality = estimator.getLinkQuality(client1, client2);
		assertTrue(quality ==  	WifiPairwiseLinkEstimator.LINK_UNESTIMATABLE);
		
		client1 = mock(Location.class);
		when(client1.getWifi()).thenReturn(new Wifis());
		client2 = mock(Location.class);
		when(client2.getWifi()).thenReturn(new Wifis());
		quality = estimator.getLinkQuality(client1, client2);
		assertTrue(quality ==  	WifiPairwiseLinkEstimator.LINK_UNESTIMATABLE);
	}
	
	
}
