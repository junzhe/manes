package org.whispercomm.manes.topology.server.linkestimation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Unit test suite for all PairwiseLinkEstimators.
 * 
 * @author Yue Liu
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ GPSPairwiseLinkEstimatorTest.class,
		WifiPairwiseLinkEstimatorTest.class })
public class EstimatorUnitSuite {
}
