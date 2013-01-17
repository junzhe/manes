package org.whispercomm.manes.client.macentity.http;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.oauth.signature.OAuthSignatureException;


public class OauthTest {
	HttpManager httpManager;
	
	@Before
	public void setUp(){
		httpManager = new HttpManager();
	}
	
	@Test
	public void testOauth(){
        LocationRequest request;
		try {
			request = new LocationRequest(0, null);
			HttpManager.signRequest(request, 0,
					"test");
	        LocationResponseHandler handler = new LocationResponseHandler(null);
	        httpManager.submit(request, handler);
		} catch (OAuthSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ManesHttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
