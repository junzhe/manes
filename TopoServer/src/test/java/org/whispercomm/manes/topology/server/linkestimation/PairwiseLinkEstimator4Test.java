package org.whispercomm.manes.topology.server.linkestimation;

import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.linkestimator.PairwiseLinkEstimator;

public class PairwiseLinkEstimator4Test implements PairwiseLinkEstimator {

	@Override
	public float getLinkQuality(Location client1, Location client2) {
		return 1;
	}

}
