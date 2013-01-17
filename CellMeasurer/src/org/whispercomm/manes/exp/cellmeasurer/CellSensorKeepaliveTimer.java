package org.whispercomm.manes.exp.cellmeasurer;

import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CellSensorKeepaliveTimer extends BroadcastReceiver {
	private static final String TAG = CellSensorKeepaliveTimer.class.getSimpleName();

	private final Context context;
	private CellSensor cellsensor;

	private final AlarmManager alarmManager;

	private final Intent intent;
	private final IntentFilter filter;
	private final PendingIntent pending;


	/**
	 * Creates a new KeepaliveSender.
	 * 
	 * @param context
	 *            the context used to get an {@link AlarmManager} instance.
	 * @param udpPacketListener
	 *            the {@code UdpPacketListener} to use to send keepalive
	 *            packets.
	 */
	public CellSensorKeepaliveTimer (Context context, CellSensor cellsensor) {
		this.cellsensor = cellsensor;
		this.context = context;
		this.alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		/*
		 * In order to disambiguate this instance's intents from those of other
		 * instances, we use a random UUID as the action.
		 */
		String action = UUID.randomUUID().toString();

		this.intent = new Intent(action);
		this.filter = new IntentFilter(action);
		this.pending = PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * The #sendKeepalive() call can block, so do it on a background thread.
		 * The phone could sleep after onReceive() exits but before the
		 * background thread completes, so to prevent that, take a wakelock
		 * before returning and release it after the background thread
		 * completes.
		 */

		new Thread(new Runnable() {
			@Override
			public void run() {
				cellsensor.getInfo();
			}
		}).start();
	}

	/**
	 * Starts the keepalive sender. Every {@code period} milliseconds, a
	 * keepalive packet will be sent by the {@code UdpPacketListener} instance.
	 * 
	 * @param period
	 *            the interval between keepalive transmissions in milliseconds.
	 */
	public void start(long period) {
		Log.v(TAG, "start() called");
		context.registerReceiver(this, filter);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), period, pending);
	}

	/**
	 * Stops the keepalive sender.
	 */
	public void stop() {
		Log.v(TAG, "stop() called");
		alarmManager.cancel(pending);
		context.unregisterReceiver(this);
	}

}
