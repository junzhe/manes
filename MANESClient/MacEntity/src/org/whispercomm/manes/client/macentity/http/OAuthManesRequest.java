package org.whispercomm.manes.client.macentity.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

import com.sun.jersey.oauth.signature.OAuthRequest;

/**
 * Wrapper class for HttpRequestBase the allows OAuth access to relevant parts
 * of messages sent between the MANES client and server that need to be hashed
 * and signed.
 * 
 * Implements the {@link OAuthRequest} interface.
 * 
 * @author David Adrian
 * 
 */
public class OAuthManesRequest implements OAuthRequest {

	HttpRequestBase httpRequest;

	public OAuthManesRequest(HttpRequestBase httpRequest) {
		this.httpRequest = httpRequest;
	}

	@Override
	public void addHeaderValue(String name, String value)
			throws IllegalStateException {
		this.httpRequest.addHeader(name, value);
	}

	@Override
	public List<String> getHeaderValues(String name) {
		Header[] headers = this.httpRequest.getHeaders(name);
		if (headers.length == 0) {
			return null;
		} else {
			List<String> values = new ArrayList<String>();
			for (Header header : headers) {
				values.add(header.getValue());
			}
			return values;
		}
	}

	/**
	 * @return empty set always since MANES does not use parameters
	 */
	@Override
	public Set<String> getParameterNames() {
		HashSet<String> nameSet = new HashSet<String>(0);
		return nameSet;
	}

	/**
	 * @return {@code null} always since MANES does not use parameters
	 */
	@Override
	public List<String> getParameterValues(String name) {
		return null;
	}

	@Override
	public String getRequestMethod() {
		return this.httpRequest.getMethod();
	}


	/**
	 * @return {@code null} if the URL is malformed
	 */
	@Override
	public URL getRequestURL() {
		try {
			return this.httpRequest.getURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
