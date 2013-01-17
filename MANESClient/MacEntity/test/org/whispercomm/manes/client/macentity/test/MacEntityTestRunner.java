package org.whispercomm.manes.client.macentity.test;

import org.junit.runners.model.InitializationError;
import org.whispercomm.manes.client.macentity.http.ShadowBase64;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

/**
 * Custom test runner to bind relevant shadow classes for Robolectric
 * 
 * @author David Adrian
 *
 */
public class MacEntityTestRunner extends RobolectricTestRunner {

	public MacEntityTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}
	
	@Override
	protected void bindShadowClasses() {
		Robolectric.bindShadowClass(ShadowBase64.class);
	}


}
