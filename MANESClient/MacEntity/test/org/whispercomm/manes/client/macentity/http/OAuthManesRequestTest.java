package org.whispercomm.manes.client.macentity.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class OAuthManesRequestTest {

	HttpRequestBase httpRequest;
	OAuthManesRequest request;

	final String name = "someHeader";
	final String value = "someValue";
	final String serverURL = "http://10.0.2.2:8080";

	@Before
	public void setUp() {
		httpRequest = new HttpPost(serverURL);
		request = new OAuthManesRequest(httpRequest);
	}

	@Test
	public void testAddHeader() {
		request.addHeaderValue(name, value);
		Header[] headers = httpRequest.getHeaders(name);
		assertEquals(headers.length, 1);
		Header header = headers[0];
		assertEquals(header.getName(), name);
		assertEquals(header.getValue(), value);
	}

	@Test
	public void testGetHeaderValuesSingle() {
		httpRequest.addHeader(name, value);
		List<String> headerList = request.getHeaderValues(name);
		assertTrue(headerList.contains(value));
		assertEquals(headerList.remove(0), value);
	}

	@Test
	public void testGetHeaderValuesMany() {
		httpRequest.addHeader(name, value);
		httpRequest.addHeader(name, value);
		httpRequest.addHeader(name, value);
		httpRequest.addHeader(name, value);
		httpRequest.addHeader(name, value);
		List<String> headerList = request.getHeaderValues(name);
		assertEquals(headerList.indexOf(this.value), 0);
		assertEquals(headerList.lastIndexOf(this.value), 4);
		for (String s : headerList) {
			assertEquals(s, this.value);
		}
	}

	@Test
	public void testGetHeaderValuesZero() {
		List<String> headerList = request.getHeaderValues(name);
		assertNull(headerList);
	}

	@Test
	public void testGetParameterNames() {
		Set<String> paramNames = request.getParameterNames();
		assertTrue(paramNames.isEmpty());
	}

	@Test
	public void testGetParameterValues() {
		List<String> values = request.getParameterValues(name);
		assertNull(values);
	}

	@Test
	public void testGetRequestMethod() {
		String method = request.getRequestMethod();
		assertEquals(method, "POST");
	}

	@Test
	public void testGetRequestURLNotNull() {
		assertNotNull(request.getRequestURL());
	}

	@Test
	public void testCanSign() {
		try {
			HttpManager.signRequest(httpRequest, "0", "1234567890abcdefghijklmnopqrstuvwxyz");
			assertTrue(true);
		} catch (OAuthSignatureException e) {
			fail("Exception thrown on signing request");
		}
	}
}
