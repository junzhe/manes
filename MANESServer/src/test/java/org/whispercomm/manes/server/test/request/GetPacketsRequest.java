package org.whispercomm.manes.server.test.request;

import java.io.OutputStream;

/**
 * 
 * @author Gulshan Singh
 * 
 */
public class GetPacketsRequest extends ExternalHttpRequest {
	private static final String URL_PATH_PATTERN = "user/%s/packet/";
	private static final String METHOD = "GET";

	private final String userId;
	private final String secret;

	public GetPacketsRequest(String options) {
		super(String.format(URL_PATH_PATTERN, parseUserId(options)), METHOD,
				null, options);
		userId = parseUserId(options);
		secret = parseSecret(options);
	}

	private static String getOption(String options, int index) {
		return options.split("\\s+")[index];
	}

	private static String parseUserId(String options) {
		return getOption(options, 0);
	}

	private static String parseSecret(String options) {
		return getOption(options, 1);
	}

	public String getSecret() {
		if (secret.equals("null"))
			return null;
		else
			return secret;
	}

	@Override
	protected void writeRequest(OutputStream stream) throws Throwable {
		stream.close();
	}

	@Override
	protected void configureConnection() throws Throwable {
		signRequest(getSecret(), userId);
	}
}
