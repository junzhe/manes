package org.whispercomm.manes.server.test.request;

/**
 * 
 * @author David R. Bild
 * 
 */
public abstract class ExternalHttpRequest extends HttpRequest {
	private static final String URL_PATTERN = "http://localhost:7889/%s";

	public ExternalHttpRequest(String path, String method, String contentType,
			String options) {
		super(String.format(URL_PATTERN, path), method, contentType, options);
	}

}
