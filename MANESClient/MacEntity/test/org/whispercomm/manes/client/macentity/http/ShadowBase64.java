package org.whispercomm.manes.client.macentity.http;

import android.util.Base64;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow implementation of android.util.Base64 for testing. Only implements
 * the static {@code encode(byte[], int)} method to return a randomly generated
 * byte array of equal length to the input.
 * 
 * @author David Adrian
 * 
 */
@Implements(Base64.class)
public class ShadowBase64 {

	@Implementation
	public static byte[] encode(byte[] input, int flags) {
		byte[] result = ManesTestUtility.generatePacketAsBytes(input.length);
		return result;
	}
}
