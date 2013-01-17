package org.whispercomm.manes.server.test.request;

import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.sun.jersey.core.util.Base64;

/**
 * 
 * @author Gulshan Singh
 * 
 */
public class SendPacketRequest extends ExternalHttpRequest {
	private static final String URL_PATH_PATTERN = "user/%s/packet/";
	private static final String METHOD = "POST";
	private static final String CONTENT_TYPE = "application/json";

	private final String userId;
	private final String contents;
	private final String secret;
	private final Long appId;

	public SendPacketRequest(String options) {
		super(String.format(URL_PATH_PATTERN, parseUserId(options)), METHOD,
				CONTENT_TYPE, options);
		userId = parseUserId(options);
		contents = parseContents(options);
		secret = parseSecret(options);
		appId = parseAppId(options);
	}

	private static String getOption(String options, int index) {
		return options.split("\\s+")[index];
	}

	private static String parseUserId(String options) {
		return getOption(options, 0);
	}

	private static Long parseAppId(String options) {
		String val = getOption(options, 1);
		if (val.equals("null"))
			return null;
		else
			return Long.valueOf(val);
	}

	private static String parseContents(String options) {
		return getOption(options, 2);
	}

	private static String parseSecret(String options) {
		return getOption(options, 3);
	}

	public String getSecret() {
		if (secret.equals("null"))
			return null;
		else
			return secret;
	}

	public Long getAppId() {
		return appId;
	}

	public String getContents() {
		if (contents.equals("null"))
			return null;
		else if (contents.equals("empty"))
			return "";
		else
			return contents;
	}

	@Override
	protected void writeRequest(OutputStream stream) throws Throwable {
		// Initialize mappers
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
		node.put("app_id", getAppId());
		if (getContents() == null)
			node.put("contents", (String) null);
		else
			node.put("contents", Base64.encode(getContents()));

		// Send JSON object to server
		mapper.writeValue(stream, node);
	}

	@Override
	protected void configureConnection() throws Throwable {
		signRequest(getSecret(), userId);
	}
}
