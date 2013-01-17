package org.whispercomm.manes.topology.location;

import static org.junit.Assert.*;

import org.junit.Test;

public class DistanceVincentyTest {

	/**
	 * This test only makes sure that re-written java code produces the same
	 * result as in the original javascript in
	 * http://www.movable-type.co.uk/scripts/latlong-vincenty.html
	 */
	@Test
	public void test() {
		assertTrue(DistanceVincenty.getGpsDistance(42.29414999485016,
				-83.70946884155273, 42.29418754577637, -83.7093722820282) - 8.989 < 0.001);
	}

}
