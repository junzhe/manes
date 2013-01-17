package org.whispercomm.manes.client.maclib;

import org.whispercomm.manes.client.maclib.ui.ManesInstaller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Utility methods for launching various MANES activities, like installation and
 * registration.
 * 
 * @author David R. Bild
 * 
 */
public class ManesActivityHelper {

	/**
	 * Launches the MANES client registration activity.
	 * 
	 * @see ClientActivity
	 * 
	 * @param context
	 *            the context used to create the registration activity
	 */
	public static void startRegistration(Context context) {
		Intent intent = new Intent(
				"org.whispercomm.manes.client.macentity.REGISTRATION");
		intent.setComponent(new ComponentName(
				"org.whispercomm.manes.client.macentity",
				"org.whispercomm.manes.client.macentity.ui.RegistrationActivity"));
		context.startActivity(intent);
	}

	/**
	 * Launches the Market application or browser to install the MANES client
	 * application.
	 * 
	 * @see ManesInstaller
	 * 
	 * @param context
	 *            the context used to create the installation activity
	 */
	public static void startInstallation(Context context) {
		Intent intent = new Intent(context, ManesInstaller.class);
		context.startActivity(intent);
	}

}
