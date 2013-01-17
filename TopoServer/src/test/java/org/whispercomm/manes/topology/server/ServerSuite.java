package org.whispercomm.manes.topology.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Complete test suite for the MANES Topology server.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { ServerUnitSuite.class, ServerIntegrationSuite.class })
public class ServerSuite {
}
