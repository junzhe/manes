package org.whispercomm.manes.client.macentity.network;

import java.io.IOException;
import java.security.SecureRandom;

import org.apache.http.client.ClientProtocolException;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.http.ManesHttpException;
import org.whispercomm.manes.client.macentity.http.RegistrationRequest;
import org.whispercomm.manes.client.macentity.http.RegistrationResponseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Manager for the information associated with the local MANES user. This class
 * is responsible for providing access to user properties (e.g., id and shared
 * secret) and performing registration with the MANES server.
 * 
 * @author David Adrian
 * @author David R. Bild
 * 
 */
public class IdManager {
	private static final String TAG = IdManager.class.getSimpleName();

	/**
	 * The number of characters in a shared secret
	 */
	private static final int SECRET_LENGTH = 100;

	/**
	 * The shared secret is generated from this character set.
	 */
	private static final String SECRET_SOURCE = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * Generates a new secret of length {@link SECRET_LENGTH} from the
	 * {@link SECRET_SOURCE} character set;
	 * 
	 * @return
	 */
	private static String generateSecret() {
		SecureRandom random = new SecureRandom();
		StringBuilder secretBuilder = new StringBuilder();
		while (secretBuilder.length() < SECRET_LENGTH) {
			int index = random.nextInt(SECRET_SOURCE.length());
			secretBuilder.append(SECRET_SOURCE.charAt(index));
		}
		return secretBuilder.toString();
	}

	/**
	 * Shared preference key for the user id
	 */
	private static final String KEY_USER_ID = "MANES_USER_ID";

	/**
	 * Shared preference key for the shared secret
	 */
	private static final String KEY_SECRET = "OAUTH_SHARED_SECRET";

	/**
	 * Shared preference key for the previous shared secret
	 */
	private static final String KEY_OLD_SECRET = "PREVIOUS_SECRET";

	/**
	 * The default {@link SharedPreferences} instance for the supplied context.
	 */
	private final SharedPreferences sharedPrefs;

	/**
	 * Creates an IdManager. Does not generate a shared secret.
	 * 
	 * @param httpManager
	 * @param context
	 *            the application context
	 * 
	 */
	public IdManager(Context context) {
		this.sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	/**
	 * Register with the MANES server, if not already registered.
	 * 
	 * @return the newly-assigned user id, or if already registered, the
	 *         existing user id
	 * @throws ManesHttpException
	 *             if the registration fails
	 */
	public long register(HttpManager httpManager) throws ManesHttpException {
		if (isRegistered()) {
			return sharedPrefs.getLong(KEY_USER_ID, -1);
		}

		Log.i(TAG, "Attempting to register with the MANES server.");

		String secret = generateSecret();
		RegistrationRequest request = new RegistrationRequest(secret);
		RegistrationResponseHandler handler = new RegistrationResponseHandler();

		Long userId;
		try {
			userId = httpManager.execute(request, handler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Registration failed.", e);
			throw new ManesHttpException(e);
		} catch (IOException e) {
			Log.e(TAG, "Registration failed.", e);
			throw new ManesHttpException(e);
		}

		if (userId != null) {
			this.updateSharedSecret(secret);
			this.setUserId(userId);
			Log.i(TAG, "Registration successful.");
			return userId;
		} else {
			Log.e(TAG, "Registration failed. Null user id returned.");
			throw new ManesHttpException(
					"Registration response handler returned null user id.");
		}
	}

	public boolean isRegistered() {
		return (-1 != sharedPrefs.getLong(KEY_USER_ID, -1));
	}

	/**
	 * Stores the shared secret used for OAuth. The previous secret (if one
	 * existed) is stored as the old secret.
	 * 
	 * @param secret
	 *            the secret shared with the MANES server
	 */
	private void updateSharedSecret(String secret) {
		Editor editor = sharedPrefs.edit();

		try {
			editor.putString(KEY_OLD_SECRET, getSharedSecret());
		} catch (NotRegisteredException e) {
			// Ignore. No old secret if not already registered.
		}
		editor.putString(KEY_SECRET, secret);

		editor.commit();
		Log.i(TAG, "Updated shared secret.");
	}

	/**
	 * Returns the shared secret used for OAuth.
	 * 
	 * @return the secret shared with the MANES server
	 * @throws NotRegisteredException
	 *             if the client is not registered with the server, so no secret
	 *             has been shared
	 */
	public String getSharedSecret() throws NotRegisteredException {
		String secret = sharedPrefs.getString(KEY_SECRET, null);
		if (secret != null)
			return secret;
		else
			throw new NotRegisteredException();
	}

	/**
	 * Stores the id for the currently-registered user. This should only be
	 * called with the id returned by the MANES server registration process.
	 * 
	 * @param userId
	 *            the id assigned by MANES Server
	 */
	private void setUserId(long userId) {
		sharedPrefs.edit().putLong(KEY_USER_ID, userId).commit();
		Log.i(TAG, String.format("Updated user id to %d.", userId));
	}

	/**
	 * Returns the id of the currently-registered user.
	 * 
	 * @return the id assigned by the MANES server
	 * @throws NotRegisteredException
	 *             if the client is not registered with the server, so no user
	 *             id has been assigned
	 */
	public long getUserId() throws NotRegisteredException {
		Long id = sharedPrefs.getLong(KEY_USER_ID, -1);
		if (id != -1)
			return id;
		else
			throw new NotRegisteredException();
	}

}
