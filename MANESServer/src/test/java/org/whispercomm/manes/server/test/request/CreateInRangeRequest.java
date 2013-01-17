package org.whispercomm.manes.server.test.request;

import java.io.OutputStream;

/**
 * 
 * @author Gulshan Singh
 * 
 */
public class CreateInRangeRequest extends InternalHttpRequest {
	private static final String URL_PATH_PATTERN = "topology/recordInRange/%s/%s/";
	private static final String METHOD = "PUT";

	public CreateInRangeRequest(String options) {
		super(String.format(URL_PATH_PATTERN, parseUser1(options),
				parseUser2(options)), METHOD, null, options);
	}

	private static String parseUser1(String options) {
		return options.split("\\s+")[0];
	}

	private static String parseUser2(String options) {
		return options.split("\\s+")[1];
	}

	@Override
	protected void writeRequest(OutputStream stream) throws Throwable {
		stream.close();
	}

	@Override
	protected void configureConnection() throws Throwable {
		// No additional configuration needed
	}

}
