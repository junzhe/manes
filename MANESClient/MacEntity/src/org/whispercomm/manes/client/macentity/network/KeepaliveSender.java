package org.whispercomm.manes.client.macentity.network;

import org.whispercomm.manes.client.macentity.util.PeriodicExecutor;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * This class sends UDP keepalive messages at a regular interval.
 * 
 * Internally, this class uses {@link PeriodicExecutor} to ensure keepalives are
 * sent even if the device is sleeping.
 * 
 * @author David R. Bild
 * 
 */
public class KeepaliveSender implements Runnable {
	private static final String TAG = KeepaliveSender.class.getSimpleName();

	private static final String PERIODIC_EXECUTOR_ACTION = "org.whispercomm.manes.client.macentity.network.UDP_KEEPALIVE";

	private final UdpPacketListener udpPacketListener;
	private final PeriodicExecutor executor;
	private final NetworkStateReceiver networkStateReceiver;
	private KeepaliveFailureHandler failureHandler;

	/**
	 * Creates a new KeepaliveSender.
	 * 
	 * @param context
	 *            the context used to get an {@link AlarmManager} instance.
	 * @param udpPacketListener
	 *            the {@code UdpPacketListener} to use to send keepalive
	 *            packets.
	 */
	public KeepaliveSender(Context context, UdpPacketListener udpPacketListener) {
		this.udpPacketListener = udpPacketListener;
		this.executor = new PeriodicExecutor(context, PERIODIC_EXECUTOR_ACTION,
				this);
		this.networkStateReceiver = new NetworkStateReceiver(context);
		this.failureHandler = null;
	}

	public void setFailureHandler(KeepaliveFailureHandler failureHandler) {
		this.failureHandler = failureHandler;
	}

	@Override
	public void run() {
		try {
			udpPacketListener.sendKeepalive();
			Log.v(TAG, "Sent keepalive.");
		} catch (KeepaliveFailureException e) {
			Log.e(TAG, "Failed to send port punching packet.", e);
			if (failureHandler != null)
				failureHandler.handle(e);
		} catch (NotRegisteredException e) {
			if (failureHandler != null)
				failureHandler.handle(e);
		}
	}

	/**
	 * Starts the keepalive sender. Every {@code period} milliseconds, a
	 * keepalive packet will be sent by the {@code UdpPacketListener} instance.
	 * 
	 * @param period
	 *            the interval between keepalive transmissions in milliseconds.
	 */
	public void start(long period) {
		Log.i(TAG, "Starting KeepaliveSender.");
		executor.start(period, true);
		networkStateReceiver.start();
	}

	/**
	 * Stops the keepalive sender.
	 */
	public void stop() {
		networkStateReceiver.stop();
		executor.stop();
		Log.i(TAG, "KeepaliveSender stopped.");
	}

	/**
	 * BroadcastReceiver that listens to network state changes in order to send
	 * a new UDP keepalive as soon as a new network is activated. This reduces
	 * downtime when switching, for example, from wifi to cellular.
	 * 
	 * @author David R. Bild
	 * 
	 */
	private class NetworkStateReceiver extends BroadcastReceiver {

		private final Context context;
		private final IntentFilter filter;
		private final ConnectivityManager connectivityManager;
		private final WakeLock wakeLock;

		public NetworkStateReceiver(Context context) {
			this.context = context;
			this.filter = new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION);
			this.connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			this.wakeLock.setReferenceCounted(true);

		}

		public void start() {
			context.registerReceiver(this, filter);
		}

		public void stop() {
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.i(TAG, e.getMessage(), e);
			}
		}

		/**
		 * Sends a UDP keepalive if a network is connected
		 */
		private void sendIfConnected() {
			NetworkInfo active = connectivityManager.getActiveNetworkInfo();
			if (active != null && active.isConnected()) {
				try {
					udpPacketListener.sendKeepalive();
					Log.v(TAG, "Sent keepalive.");
				} catch (KeepaliveFailureException e) {
					Log.e(TAG, "Failed to send port punching packet.", e);
				} catch (NotRegisteredException e) {
					Log.e(TAG, "Client is not registered yet.", e);
				}
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			wakeLock.acquire();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						sendIfConnected();
					} finally {
						wakeLock.release();
					}
				}
			}).start();
		}
	}
}
