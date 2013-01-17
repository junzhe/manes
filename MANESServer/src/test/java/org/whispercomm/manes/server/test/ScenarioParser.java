package org.whispercomm.manes.server.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whispercomm.manes.server.test.scenario.ManesScenario;
import org.whispercomm.manes.server.test.scenario.Scenario;

/**
 * 
 * @author David R. Bild
 * 
 */
public class ScenarioParser {

	/**
	 * Parses test files for HTTP requests
	 * 
	 * @throws Throwable
	 */
	public static List<Scenario> parse(String resourceName) throws Throwable {
		List<Scenario> scenarios = new LinkedList<Scenario>();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResourceAsStream(resourceName)));

		Pattern pattern = Pattern.compile("@Description=\"(.*)\"");
		Matcher matcher = pattern.matcher("");

		String description = null;
		StringBuilder scenarioLines = null;
		Integer firstLine = null;

		String line;
		int lineNum = 0;
		while ((line = br.readLine()) != null) {
			lineNum++;
			line = line.trim();

			if (line.length() == 0) {
				/* Skip blank lines */
				continue;
			} else if (line.startsWith("#")) {
				/* Skip comments */
				continue;
			} else if (matcher.reset(line).find()) {
				/* Description starts new Scenario */
				// Build the previous scenario, if it exists
				if (scenarioLines != null) {
					scenarios.add(new ManesScenario(description, scenarioLines
							.toString(), resourceName, firstLine));
				}

				// Reset for new scenario
				firstLine = null;
				scenarioLines = null;
				description = matcher.group(1);
			} else {
				/* Add the line to the under-construction scenario */
				// If first line, record
				if (scenarioLines == null) {
					scenarioLines = new StringBuilder();
					firstLine = lineNum;
				}
				scenarioLines.append('\n').append(line);
			}
		}
		// Build the last scenario, if needed
		if (scenarioLines != null) {
			scenarios.add(new ManesScenario(description, scenarioLines
					.toString(), resourceName, firstLine));
		}

		return scenarios;
	}
}
