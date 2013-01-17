package org.whispercomm.manes.client.macentity.terms;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class AgreementManager {
	protected static final String TAG = AgreementManager.class.getSimpleName();

	private static final String KEY_AGREED = "has_agreed";

	public static boolean hasAgreed(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean agreed = appSharedPrefs.getBoolean(KEY_AGREED, false);
		return agreed;
	}

	public static void recordUserAgreement(Context context) {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = appSharedPrefs.edit();
		editor.putBoolean(KEY_AGREED, true);
		editor.commit();
		Log.i(TAG, "User accepted the usage agreement.");
	}
}
