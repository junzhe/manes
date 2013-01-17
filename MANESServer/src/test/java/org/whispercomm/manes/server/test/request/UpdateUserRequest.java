package org.whispercomm.manes.server.test.request;

import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * @author Gulshan Singh
 * 
 */
public class UpdateUserRequest extends ExternalHttpRequest {
	private static final String URL_PATH_PATTERN = "user/%s/";
	private static final String METHOD = "PUT";
	private static final String CONTENT_TYPE = "application/json";

	private final String userId;
	private final String oldSecret;
	private final String secret;
	private final String c2dmId;

	public UpdateUserRequest(String options) {
		super(String.format(URL_PATH_PATTERN, parseUserId(options)), METHOD,
				CONTENT_TYPE, options);
		userId = parseUserId(options);
		oldSecret = parseOldSecret(options);
		secret = parseSecret(options);
		c2dmId = parseC2dmId(options);
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

	private static String parseC2dmId(String options) {
		return getOption(options, 2);
	}

	private static String parseOldSecret(String options) {
		return getOption(options, 3);
	}

	public String getSecret() {
		if (secret.equals("null"))
			return null;
		else
			return secret;
	}

	public String getC2DMId() {
		if (c2dmId.equals("null"))
			return null;
		else if (c2dmId.equals("empty"))
			return "";
		else
			return c2dmId;
	}

	@Override
	protected void writeRequest(OutputStream stream) throws Throwable {
		// Initialize mappers
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
		node.put("secret", getSecret());
		node.put("c2dm_reg_id", getC2DMId());

		// Send JSON object to server
		mapper.writeValue(stream, node);
	}

	@Override
	protected void configureConnection() throws Throwable {
		signRequest(oldSecret, userId);
	}

}
