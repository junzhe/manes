package org.whispercomm.manes.exp.locationsensor.server.http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class RecordingIdGenerator {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RecordingIdGenerator.class);

	private static final String DEFAULT_PATH = "/var/locsensor/id/id.log";
	private static final Charset CHARSET = Charset.forName("US-ASCII");

	private static final byte[] START_BYTES = new String("-1")
			.getBytes(CHARSET);

	private File file;
	
	public RecordingIdGenerator() {
		this(DEFAULT_PATH);
	}

	public RecordingIdGenerator(String path) {
		file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
				Files.write(START_BYTES, file);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				throw new RuntimeException(
						"Could not create identifier record file");
			}
		}
	}

	public synchronized int getNextIdentifier() {
		try {
			String line = Files.readFirstLine(file, CHARSET);
			int id = Integer.valueOf(line);
			id++;
			byte[] next = Integer.toString(id).getBytes(CHARSET);
			Files.write(next, file);
			return id;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException("Could not read identifier record file");
		}
	}

}
