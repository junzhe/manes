package org.whispercomm.manes.topology.server.linkestimator;

import java.util.HashMap;
import java.util.Map;

import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GpsGridElement;
import org.whispercomm.manes.topology.location.Location;

//TODO replace with LOOK_UP with more accurate predictions.
public class GPSPairwiseLinkEstimator implements PairwiseLinkEstimator {

	static final double STEP_SIZE = GpsGridElement.DISTANCE_NO_SIGNAL;// in meters
	static final int DISTANCE_INDEX_MIN = 0;
	static final int DISTANCE_INDEX_MAX = 1;
	@SuppressWarnings("serial")
	static final Map<Integer, Float> LOOK_UP = new HashMap<Integer, Float>() {
		{
			// always connectable if distance is smaller than
			// GPSGrid.DISTANCE_NO_SIGNA (250m), and always
			// not connectable if distance is larger than
			// GPSGrid.DISTANCE_NO_SIGNA (250m).
			put(Integer.valueOf(0), Float.valueOf(1));
			put(Integer.valueOf(1), Float.valueOf(0));
		}
	};

	@Override
	public float getLinkQuality(Location client1, Location client2) {
		if (client1 == null || client2 == null)
			return LINK_UNESTIMATABLE;
		// get distance
		GPS gps1 = client1.getGps();
		GPS gps2 = client2.getGps();
		if (gps1 == null || gps2 == null)
			return LINK_UNESTIMATABLE;
		double distance = GPS.getDistance(gps1, gps2);
		// get distance index
		int distanceIndex = (int) (distance / STEP_SIZE);
		if (distanceIndex < DISTANCE_INDEX_MIN) {
			distanceIndex = DISTANCE_INDEX_MIN;
		} else if (distanceIndex > DISTANCE_INDEX_MAX) {
			distanceIndex = DISTANCE_INDEX_MAX;
		}
		// look up link quality (Packet Reception Rate)
		float prr = LOOK_UP.get(Integer.valueOf(distanceIndex));

		return prr;
	}

}
