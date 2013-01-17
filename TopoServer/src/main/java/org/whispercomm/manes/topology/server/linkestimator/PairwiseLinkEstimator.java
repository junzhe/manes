package org.whispercomm.manes.topology.server.linkestimator;

import org.whispercomm.manes.topology.location.Location;

/**
 * Produce estimation for a given pair of clients with corresponding
 * {@link Location} information.
 * 
 * @author Yue Liu
 * 
 */
public interface PairwiseLinkEstimator {

	/**
	 * Unable to estimate the link quality because of insufficient information.
	 */
	public static final float LINK_UNESTIMATABLE = -1;

	/**
	 * Estimate link quality for two clients.
	 * 
	 * @param client1
	 * @param client2
	 * @return link quality as the connection probability, i.e., the probability
	 *         that one packet can be successfully transmitted between the
	 *         clients. Return LINK_UNESTMATABLE if there is not enough
	 *         information for a successful estimation.
	 */
	public float getLinkQuality(Location client1, Location client2);
}
