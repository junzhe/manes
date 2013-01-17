package org.whispercomm.manes.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Complete test suite for the MANES server.
 * 
 * @author David R. Bild
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { ServerUnitSuite.class, ServerIntegrationSuite.class })
public class ServerSuite {
}
