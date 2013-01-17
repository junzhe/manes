package org.whispercomm.manes.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.whispercomm.manes.server.http.HttpUnitSuite;

/**
 * Test suite for all unit tests for the MANES server
 * <p>
 * Each new test suite must be manually added to the @Suite.SuiteClasses
 * annotation below.
 * 
 * @author David R. Bild
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ HttpUnitSuite.class })
public class ServerUnitSuite {
}
