package org.whispercomm.manes.topology.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.whispercomm.manes.topology.server.data.DataInterfaceVoldemortTest;
import org.whispercomm.manes.topology.server.http.HttpUnitSuite;
import org.whispercomm.manes.topology.server.linkestimation.EstimatorUnitSuite;
import org.whispercomm.manes.topology.server.logger.LoggerTest;
import org.whispercomm.manes.topology.server.manager.LocationUpdateHandlerTest;
import org.whispercomm.manes.topology.server.topoestimator.TopoEstimatorUnitSuite;

/**
 * Test suite for all unit tests for the MANES Topology server
 * <p>
 * Each new test suite must be manually added to the @Suite.SuiteClasses
 * annotation below.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ DataInterfaceVoldemortTest.class,
		EstimatorUnitSuite.class, HttpUnitSuite.class,
		LoggerTest.class, LocationUpdateHandlerTest.class,
		TopoEstimatorUnitSuite.class })
public class ServerUnitSuite {
}
