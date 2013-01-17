package org.whispercomm.manes.server.test.request;

/**
 * 
 * @author David R. Bild
 * 
 */
public abstract class InternalHttpRequest extends HttpRequest {
	private static final String URL_PATTERN = "http://localhost:6889/internal/%s";

	public InternalHttpRequest(String path, String method, String contentType,
			String options) {
		super(String.format(URL_PATTERN, path), method, contentType, options);
	}

}
