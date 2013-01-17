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
public class CreateUserRequest extends ExternalHttpRequest {
	private static final String URL_PATH = "user/";
	private static final String METHOD = "POST";
	private static final String CONTENT_TYPE = "application/json";

	private final String secret;
	private final String c2dmId;

	public CreateUserRequest(String options) {
		super(URL_PATH, METHOD, CONTENT_TYPE, options);
		this.secret = parseSecret(options);
		this.c2dmId = parseC2DMId(options);
	}

	private static String parseSecret(String options) {
		return options.split("\\s+")[0];
	}

	private static String parseC2DMId(String options) {
		return options.split("\\s+")[1];
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
	public void writeRequest(OutputStream stream) throws Throwable {
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
		// No additional configuration needed
	}
}
