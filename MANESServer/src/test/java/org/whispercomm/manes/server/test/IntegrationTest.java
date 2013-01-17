package org.whispercomm.manes.server.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.whispercomm.manes.server.DevelopmentMain;
import org.whispercomm.manes.server.test.scenario.Scenario;
import org.whispercomm.manes.server.test.scenario.Scenarios;
import org.whispercomm.manes.server.test.scenario.runner.ScenarioRunner;

@RunWith(ScenarioRunner.class)
public class IntegrationTest {
	/**
	 * Instance of development server to test
	 */
	private static DevelopmentMain main;

	@Before
	public static void startServer() throws Exception {
		main = new DevelopmentMain();
		main.start();
		if (!main.await())
			throw new RuntimeException(
					"Failed to start DevelopmentMain server.");
	}

	@After
	public static void stopServer() throws Exception {
		main.stop();
	}

	@Scenarios("Create Users")
	public static List<Scenario> createUserTests() throws Throwable {
		return ScenarioParser.parse("integration-tests/user-tests");
	}

	@Scenarios("Update Users")
	public static List<Scenario> updateUserTests() throws Throwable {
		return ScenarioParser.parse("integration-tests/update-user-tests");
	}

	@Scenarios("Send/Receive Packets")
	public static List<Scenario> sendReceiveTests() throws Throwable {
		return ScenarioParser.parse("integration-tests/send-receive-tests");
	}

}
