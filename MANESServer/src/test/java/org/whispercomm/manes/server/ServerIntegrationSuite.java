package org.whispercomm.manes.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.whispercomm.manes.server.http.HttpIntegrationSuite;

/**
 * Test suite for all integration tests for the MANES server.
 * <p>
 * Each new test suite must be manually added to the @Suite.SuiteClasses
 * annotation below.
 * 
 * @author David R. Bild
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ HttpIntegrationSuite.class })
public class ServerIntegrationSuite {
}
