package org.whispercomm.manes.exp.locationsensor.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.jersey.spi.service.ServiceFinder.ServiceIteratorProvider;

import android.util.Log;

/**
 * Modified from Buscador class posted to <a
 * href=http://jersey.576304.n2.nabble.
 * com/java-lang-NullPointerException-on-Android-td4212447.html>Jersey
 * listserv</a>
 * 
 * @author Buscador
 * @author David R. Bild
 */
public class OAuthSignatureMethodServiceProvider<T> extends
		ServiceIteratorProvider<T> {
	private static final String TAG = OAuthSignatureMethodServiceProvider.class
			.getName();

	private static final HashMap<String, String[]> SERVICES = new HashMap<String, String[]>();

	private static final String[] com_sun_jersey_oauth_signature_OAuthSignatureMethod = new String[] {
			"com.sun.jersey.oauth.signature.HMAC_SHA1",
			"com.sun.jersey.oauth.signature.PLAINTEXT",
			"com.sun.jersey.oauth.signature.RSA_SHA1" };

	static {
		SERVICES.put("com.sun.jersey.oauth.signature.OAuthSignatureMethod",
				com_sun_jersey_oauth_signature_OAuthSignatureMethod);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Class<T>> createClassIterator(Class<T> service,
			String serviceName, ClassLoader loader,
			boolean ignoreOnClassNotFound) {
		String[] classesNames = SERVICES.get(serviceName);
		int length = classesNames.length;
		ArrayList<Class<T>> classes = new ArrayList<Class<T>>(length);
		for (int i = 0; i < length; i++) {
			try {
				classes.add((Class<T>) Class.forName(classesNames[i]));
			} catch (ClassNotFoundException e) {
				Log.e(TAG, String.format("Failed to add class to iterator: %s",
						classesNames[i]), e);
			}
		}
		return classes.iterator();
	}

	@Override
	public Iterator<T> createIterator(Class<T> service, String serviceName,
			ClassLoader loader, boolean ignoreOnClassNotFound) {
		String[] classesNames = SERVICES.get(serviceName);
		int length = classesNames.length;
		ArrayList<T> classes = new ArrayList<T>(length);
		for (int i = 0; i < length; i++) {
			try {
				classes.add(service.cast(Class.forName(classesNames[i])
						.newInstance()));
			} catch (IllegalAccessException e) {
				Log.e(TAG, String.format(
						"Failed to create instance of class: %s",
						classesNames[i]), e);
			} catch (InstantiationException e) {
				Log.e(TAG, String.format(
						"Failed to create instance of class: %s",
						classesNames[i]), e);
			} catch (ClassNotFoundException e) {
				Log.e(TAG, String.format(
						"Failed to create instance of class: %s",
						classesNames[i]), e);
			}
		}
		return classes.iterator();
	}
}