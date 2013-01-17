package org.whispercomm.manes.client.maclib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

/**
 * A {@link BroadcastReceiver} for receiving a {@link ManesInstallationListener}
 * callback when the Manes client is installed.
 * 
 * @author David R. Bild
 * 
 */
public class ManesInstallationReceiver extends BroadcastReceiver {
	private static final String TAG = ManesInstallationReceiver.class
			.getSimpleName();

	private static final IntentFilter filter;
	static {
		filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
	}

	private final Context context;
	private final ManesInstallationListener listener;

	/**
	 * Creates and registers a new ManesInstalaltionReceiver instance. The
	 * specified ManesInstallationListener will be notified when the Manes
	 * client is installed.
	 * <p>
	 * The caller is responsible for calling the {@link #stop()} method on the
	 * returned instance before the provided {@code Context} is destroyed.
	 * 
	 * @param context
	 *            the context used to register the {@code BroadcastReceiver}
	 * @param listener
	 *            the callback to be invoked when the Manes client is installed
	 * @return the new receiver.
	 */
	public static ManesInstallationReceiver start(Context context,
			ManesInstallationListener listener) {
		ManesInstallationReceiver receiver = new ManesInstallationReceiver(
				context, listener);
		context.registerReceiver(receiver, filter);
		return receiver;
	}

	private ManesInstallationReceiver(Context context,
			ManesInstallationListener listener) {
		this.context = context;
		this.listener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Uri uri = intent.getData();
		if (uri != null
				&& uri.getSchemeSpecificPart().equals(
						"org.whispercomm.manes.client.macentity")) {
			Log.i(TAG, "Manes client installed intent received.");
			listener.manesInstalled();
		}
	}

	/**
	 * Unregisters this {@code BroadcastReceiver}.
	 */
	public void stop() {
		context.unregisterReceiver(this);
	}

}
