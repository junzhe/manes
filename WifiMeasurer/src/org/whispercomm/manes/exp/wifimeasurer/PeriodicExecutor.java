package org.whispercomm.manes.exp.wifimeasurer;

import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * A helper class for periodically executing a {@link Runnable}, even if the
 * device is in suspend. Internally, the {@link AlarmManager} feature of Android
 * is used.
 * <p>
 * This class is intended to replace Java code of the following form:
 * 
 * <pre>
 * <code>
 * int period = 1000;
 * Runnable myWork = new MyWork();
 * volatile boolean running = true;
 * 
 * new Thread(new Runnable() {
 *     public void run() {
 *         while (running) {
 *             myWork.run();
 *             Thread.sleep(period);
 *         }
 *     }
 * }).start();
 * 
 * // Sometime later
 * running = false;
 * </code>
 * </pre>
 * 
 * with code like this:
 * 
 * <pre><code>
 * int period = 1000;
 * Runnable myWork = new MyWork();
 * PeriodicExecutor executor = new PeriodicExecutor(context, myWork);
 * executor.start(period);
 * 
 * // Sometime later
 * executor.stop();
 * </code></pre>
 * 
 * @author David R. Bild
 * 
 */
public class PeriodicExecutor {
	private static final String TAG = PeriodicExecutor.class.getSimpleName();

	private final Context context;
	private final AlarmManager alarmManager;

	private final Intent intent;
	private final IntentFilter filter;
	private final PendingIntent pending;

	private final Receiver receiver;

	/**
	 * Creates a new PeriodicExecutor for the supplied {@code Runnable}.
	 * <p>
	 * The phone is guaranteed to stay awake only until the
	 * {@link Runnable#run()} method returns. If it needs to stay awake longer,
	 * you must manage your own wakelocks.
	 * 
	 * @param context
	 *            the {@code Context} used to access the {@link AlarmManager}.
	 * @param runnable
	 *            the runnable to execute periodically.
	 */
	public PeriodicExecutor(Context context, Runnable runnable) {
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

		this.receiver = new Receiver(runnable);
	}

	/**
	 * Starts the periodic executor. Every {@code period} milliseconds, the
	 * {@link Runnable#run() run()} method of the supplied {@code Runnable} is
	 * invoked.
	 * <p>
	 * This call must be <i>must</i> be paired with a call to {@link #stop()}.
	 * 
	 * @param period
	 *            the interval between executions.
	 * @param inexact
	 *            {@code true} if the trigger time can be adjusted by Android to
	 *            align with already-scheduled wakeups or {@code false} if the
	 *            trigger times should be exact.
	 * 
	 * @see AlarmManager#setInexactRepeating(int, long, long, PendingIntent)
	 */
	public void start(long period, boolean inexact) {
		Log.v(TAG, "start() called");
		context.registerReceiver(receiver, filter);
		if (inexact) {
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), period, pending);
		} else {
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), period, pending);
		}
	}

	/**
	 * Stops the periodic executor.
	 */
	public void stop() {
		Log.v(TAG, "stop() called");
		alarmManager.cancel(pending);
		context.unregisterReceiver(receiver);
	}

	/**
	 * {@code BroadcastReceiver} subclass that is invoked by the AlarmManger.
	 * 
	 * @author David R. Bild
	 * 
	 */
	private class Receiver extends BroadcastReceiver {

		private final Runnable runnable;

		private final WakeLock wakeLock;

		public Receiver(Runnable runnable) {
		    this.runnable = runnable;

		    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		    this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		    this.wakeLock.setReferenceCounted(true);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * The #run() call can block, so do it on a background thread. The
			 * phone could sleep after onReceive() exits but before the
			 * background thread completes, so to prevent that, take a wakelock
			 * before returning and release it after the background thread
			 * completes.
			 */
			wakeLock.acquire();
			new Thread(new Runnable() {
				@Override
				public void run() {
					runnable.run();
					wakeLock.release();
				}
			}).start();
		}

	}

}
