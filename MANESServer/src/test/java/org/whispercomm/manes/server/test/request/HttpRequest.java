package org.whispercomm.manes.server.test.request;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whispercomm.manes.server.test.OAuthRequestImpl;

import com.sun.jersey.oauth.signature.HMAC_SHA1;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthRequest;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;

/**
 * Base class for HTTP commands in an integration test scenario.
 * 
 * @author David R. Bild
 * 
 */
public abstract class HttpRequest implements Request {
	private final String url;
	private final String method;
	private final String contentType;

	private final Integer expectedStatusCode;
	private final String expectedResponseBody;

	protected HttpURLConnection conn;

	public HttpRequest(String url, String method, String contentType,
			String options) {
		this.url = url;
		this.method = method;
		this.contentType = contentType;
		this.expectedStatusCode = parseExpectedStatusCode(options);
		this.expectedResponseBody = parseExpectedResponseBody(options);
	}

	private static Integer parseExpectedStatusCode(String options) {
		Pattern pattern = Pattern.compile("\\s--s\\s+(\\d+)(\\s|$)");
		Matcher m = pattern.matcher(options);
		if (m.find())
			return Integer.valueOf(m.group(1));
		else
			return null;
	}

	private static String parseExpectedResponseBody(String options) {
		Pattern pattern = Pattern.compile("\\s--r\\s+'(.+)'(\\s|$)");
		Matcher m = pattern.matcher(options);
		if (m.find()) {
			return m.group(1);
		} else
			return null;
	}

	/**
	 * Writes the request body to the output stream.
	 * 
	 * @param stream
	 * @throws Throwable
	 */
	protected abstract void writeRequest(OutputStream stream) throws Throwable;

	/**
	 * Do any additional configuration on the underlying
	 * {@link HttpURLConnection}. For example, the method might call
	 * {@link #signRequest(String, String)} to sign the request.
	 * 
	 * @throws Throwable
	 */
	protected abstract void configureConnection() throws Throwable;

	@Override
	public void execute() throws Throwable {
		// Configure the connection
		conn = (HttpURLConnection) new URL(url).openConnection();
		if (method.equals("POST") || method.equals("PUT"))
			conn.setDoOutput(true);
		conn.setRequestMethod(method);
		if (contentType != null)
			conn.setRequestProperty("Content-Type", contentType);

		// Allow to the child to do any desired configuration
		configureConnection();

		// Execute the request
		if (method.equals("POST") || method.equals("PUT"))
			writeRequest(conn.getOutputStream());
		conn.getResponseCode(); // Hack to force the request to execute.

		// and validate
		validate();
	}

	protected InputStream getResponseStream() throws IOException {
		if (conn.getResponseCode() < 400)
			return conn.getInputStream();
		else
			return conn.getErrorStream();
	}

	protected void validate() throws Throwable {
		if (expectedStatusCode != null)
			assertThat("Response status code", conn.getResponseCode(),
					is(expectedStatusCode));

		if (expectedResponseBody != null) {
			/*
			assertThat("Response body", IOUtils.toString(getResponseStream()),
					matchesRegex(expectedResponseBody, Pattern.COMMENTS));
					*/
		}
	}

	protected void signRequest(String secret, String key)
			throws OAuthSignatureException {
		OAuthRequest oauthRequest = new OAuthRequestImpl(conn);
		OAuthParameters oauthParams = new OAuthParameters().consumerKey(key)
				.token(null).signatureMethod(HMAC_SHA1.NAME).timestamp()
				.nonce().version();
		OAuthSecrets oauthSecrets = new OAuthSecrets().consumerSecret(secret)
				.tokenSecret(null);
		OAuthSignature.sign(oauthRequest, oauthParams, oauthSecrets);
	}

}
