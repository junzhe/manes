package org.whispercomm.manes.client.macentity.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.http.HttpManager;

import android.app.Activity;
import android.content.Context;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class IdManagerTest {
	
	private IdManager idManager;
	private Context context;
	private HttpManager httpManager;
	
	private static final String SECRET_SOURCE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	@Before
	public void setUp() {
		context = new Activity();
		httpManager = new HttpManager();
		idManager = new IdManager(httpManager, context);
	}
	
	@After
	public void takeDown() {
		context = null;
		httpManager = null;
		idManager = null;		
	}
	
	@Test
	public void testGenerateSecret() {
		String firstSecret = idManager.generateSecret();
		assertTrue(checkSecretParameters(firstSecret));
		String secondSecret = idManager.generateSecret();
		assertTrue(checkSecretParameters(secondSecret));
		
		assertFalse(firstSecret.equals(secondSecret));
	}
	
	public void testUpdateSecret() {
		String secret = idManager.generateSecret();
		assertTrue(idManager.getSecret() == null);
		idManager.updateSecret(secret);
		assertEquals(idManager.getSecret(), secret);
		String dummySecret = "herp";
		idManager.updateSecret(dummySecret);
		assertEquals(idManager.getSecret(), dummySecret);
	}
	
	private boolean checkSecretParameters(String secret) {
		assertTrue(secret.length() >= 32 && secret.length() <= 100);
		for (int i = 0; i < secret.length(); i++) {
			char c = secret.charAt(i);
			int j;
			for (j = 0; j < SECRET_SOURCE.length(); j++) {
				if (c == SECRET_SOURCE.charAt(j)) {
					break;
				}
			}
			if (j == SECRET_SOURCE.length()) {
				return false;
			}
		}
		return true;
	}

}
