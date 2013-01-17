package org.whispercomm.manes.client.macentity.network;

import java.util.concurrent.locks.ReentrantLock;

import org.whispercomm.manes.client.macentity.location.ManesLocationManager;
import org.whispercomm.manes.client.macentity.util.TimedExecutor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

/**
 * This class monitors network connectivity and turn on/off keepalive-sender and
 * manes-location-manager accordingly.
 * 
 * @author Yue Liu
 * 
 */
public class NetworkConnectivityMonitor extends BroadcastReceiver implements
		KeepaliveFailureHandler {

	public static final String TAG = NetworkConnectivityMonitor.class
			.getSimpleName();

	/**
	 * The wait time to confirm network disconnection. In other words, it is the
	 * maximum wait time for network reconnection after an event of network
	 * disconnected. If the network does not become reconnected after this
	 * amount of time, officially decide we are entering a state of network
	 * disconnection.
	 */
	private static final long NETWORK_DISCONNECTION_CONFIRM_PERIOD = 1 * 60 * 1000;

	private final Context context;
	private final KeepaliveSender keepaliveSender;
	private final ManesLocationManager locManager;
	private ConnectivityManager connManager;
	private boolean isConfirmingNetworkDisconnection;
	private boolean isServiceStopped;
	private Handler mHandler;
	private TimedExecutor executor;
	private String stopperId;
	private ManesLocManagerStopper stopper;
	private ReentrantLock networkStatusLock;

	public NetworkConnectivityMonitor(Context context,
			KeepaliveSender keepaliveSender, ManesLocationManager locManager) {
		this.context = context;
		this.keepaliveSender = keepaliveSender;
		this.locManager = locManager;
		this.connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.isConfirmingNetworkDisconnection = false;
		this.isServiceStopped = false;
		this.mHandler = new Handler();
		this.networkStatusLock = new ReentrantLock();
		this.executor = new TimedExecutor(context);
		this.stopper = new ManesLocManagerStopper();
		this.stopperId = null;
	}

	@Override
	public void handle(Throwable e) {
		networkStatusLock.lock();
		if (isConfirmingNetworkDisconnection) {
			// Do nothing if we have already started the process of
			// confirming network disconnection.
		} else {
			if (isNetworkConnected() == false) {
				Log.i(System.currentTimeMillis() + TAG,
						"Lost Network connection.");
				// Start the process of confirming network disconnection.
				isConfirmingNetworkDisconnection = true;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						registerNetworkConnectivityUpdates();
					}
				});
				// Schedule the stop of ManesLocationManager.
				stopperId = executor.schedule(System.currentTimeMillis()
						+ NETWORK_DISCONNECTION_CONFIRM_PERIOD, stopper);
			}
		}
		networkStatusLock.unlock();
	}

	/**
	 * This is for receiving network connectivity updates.
	 */
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		networkStatusLock.lock();
		if (isNetworkConnected()) {
			Log.i(TAG, "Network becomes connected again.");
			if (isConfirmingNetworkDisconnection && stopperId != null) {
				// Log.i(TAG,
				// "***Exit network disconnection confirming process.");
				executor.cancel(stopperId);
				stopperId = null;
				isConfirmingNetworkDisconnection = false;
			} else if (isServiceStopped) {
				Log.i(TAG,
						"Restart manes location manager and KeepaliveSender.");
				keepaliveSender.start(UdpPacketListener.PERIOD_MS);
				locManager.start();
				isServiceStopped = false;
			}
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		networkStatusLock.unlock();
	}

	/**
	 * Decide whether we are currently connected to a Wifi or Mobile network
	 * (e.g., 2G, 3G, 4G...).
	 * 
	 * @return
	 */
	private boolean isNetworkConnected() {
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo == null)
			return false;
		if (networkInfo.isConnected() == false)
			return false;
		int type = networkInfo.getType();
		if (type == ConnectivityManager.TYPE_MOBILE
				|| type == ConnectivityManager.TYPE_WIFI
				|| type == ConnectivityManager.TYPE_WIMAX)
			return true;
		else
			return false;
	}

	/**
	 * Register to listen for network connectivity changes.
	 */
	private void registerNetworkConnectivityUpdates() {
		IntentFilter intentFilter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(this, intentFilter);
	}

	/**
	 * This {@link Runnable} would cause the main thread to stop
	 * {ManesLocationManager}.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class ManesLocManagerStopper implements Runnable {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					networkStatusLock.lock();
					Log.i(System.currentTimeMillis() + TAG,
							"Confirmed network disconnection.");
					isConfirmingNetworkDisconnection = false;
					isServiceStopped = true;
					locManager.stop();
					keepaliveSender.stop();
					networkStatusLock.unlock();
				}
			});
		}

	}
}
