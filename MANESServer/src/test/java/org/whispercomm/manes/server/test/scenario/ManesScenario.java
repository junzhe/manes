package org.whispercomm.manes.server.test.scenario;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.whispercomm.manes.server.test.request.CreateInRangeRequest;
import org.whispercomm.manes.server.test.request.CreateUserRequest;
import org.whispercomm.manes.server.test.request.GetPacketsRequest;
import org.whispercomm.manes.server.test.request.HttpRequest;
import org.whispercomm.manes.server.test.request.SendPacketRequest;
import org.whispercomm.manes.server.test.request.UpdateUserRequest;

/**
 * 
 * @author Gulshan Singh
 * 
 */
public class ManesScenario implements Scenario {
	private static final Map<String, Class<? extends HttpRequest>> MAPPING = new HashMap<String, Class<? extends HttpRequest>>();
	static {
		MAPPING.put("CreateUser", CreateUserRequest.class);
		MAPPING.put("CreateInRange", CreateInRangeRequest.class);
		MAPPING.put("SendPacket", SendPacketRequest.class);
		MAPPING.put("GetPackets", GetPacketsRequest.class);
		MAPPING.put("UpdateUser", UpdateUserRequest.class);
	}

	/**
	 * Name shown in JUnit {@link Description}.
	 */
	private final String name;

	/**
	 * List of requests to be executed in the scenario
	 */
	private final List<HttpRequest> requests;

	/**
	 * Name of the file defining this scenario
	 */
	private final String sourceFile;

	/**
	 * Line number in the source file of the first line
	 */
	private final Integer firstLineNumber;

	/**
	 * Creates a new scenario for the described text.
	 */
	public ManesScenario(String name, String definition, String sourceFile,
			Integer firstLineNumber) throws Throwable {
		this.name = name;
		this.sourceFile = sourceFile;
		this.firstLineNumber = firstLineNumber;
		this.requests = parseDefinition(definition);
	}

	public ManesScenario(String name, String definition) throws Throwable {
		this(name, definition, null, null);
	}

	private static List<HttpRequest> parseDefinition(String definition)
			throws Throwable {
		List<HttpRequest> requests = new LinkedList<HttpRequest>();
		for (String line : definition.trim().split("[\\r?\\n]+")) {
			String[] split = line.trim().split("\\s+", 2);
			requests.add(createInstance(split[0], split[1]));
		}
		return requests;
	}

	private static HttpRequest createInstance(String type, String options)
			throws Throwable {
		Class<? extends HttpRequest> klass = MAPPING.get(type);
		if (klass == null)
			throw new IllegalArgumentException(String.format(
					"%s is not a valid HttpRequest type.", type));
		else
			return klass.getDeclaredConstructor(String.class).newInstance(
					options);
	}

	public String getName() {
		return name;
	}

	public void run() throws Throwable {
		int lineNum = 0;
		for (HttpRequest request : requests) {
			++lineNum;
			try {
				request.execute();
			} catch (AssertionError e) {
				String msg = String.format("line %d (%s:%d): %s", lineNum,
						this.sourceFile, this.firstLineNumber + lineNum - 1,
						e.getMessage());
				throw new AssertionError(msg);
			}
		}
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(this.getClass(),
				(name != null) ? name : "Name not set");
	}
}
