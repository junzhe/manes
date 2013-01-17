package org.whispercomm.manes.server.http;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all unit tests in the
 * {@link org.whispercomm.manes.server.http} package.
 * <p>
 * Each new test class must be manually added to the @Suite.SuiteClasses
 * annotation below.
 * 
 * @author David R. Bild
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ UserResourceUnitTest.class, PacketResourceUnitTest.class })
public class HttpUnitSuite {
}
