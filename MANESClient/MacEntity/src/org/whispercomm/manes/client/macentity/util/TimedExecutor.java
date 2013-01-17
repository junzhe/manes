package org.whispercomm.manes.client.macentity.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

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
 * A helper class for executing a {@link Runnable} at specified time, even if
 * the device is in suspend. Internally, the {@link AlarmManager} feature of
 * Android is used.
 * 
 * @author Yue Liu
 * 
 */
public class TimedExecutor {

	public static final String TAG = TimedExecutor.class.getSimpleName();

	private final Context context;
	private final AlarmManager alarmManager;
	/**
	 * Stores the execution times of all the pending jobs in a priority queue to
	 * keep track of the earliest job.
	 */
	private PriorityBlockingQueue<TimedExecutorJob> pendingTimes;
	/**
	 * Store all pending jobs in a map.
	 */
	private ConcurrentHashMap<String, TimedExecutorJob> pendingJobs;
	private LinkedBlockingQueue<Thread> runningThreads;
	private boolean shutDown;
	private ReentrantLock shutdownLock;

	/**
	 * Creates a new TimedExecutor.
	 * <p>
	 * The phone is guaranteed to stay awake only until the
	 * {@link Runnable#run()} method returns. If it needs to stay awake longer,
	 * you must manage your own wakelocks.
	 * 
	 * @param context
	 *            the {@code Context} used to access the {@link AlarmManager}.
	 */
	public TimedExecutor(Context context) {
		this.context = context;
		this.alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		this.pendingJobs = new ConcurrentHashMap<String, TimedExecutorJob>();
		this.pendingTimes = new PriorityBlockingQueue<TimedExecutorJob>();
		this.runningThreads = new LinkedBlockingQueue<Thread>();
		this.shutDown = false;
		this.shutdownLock = new ReentrantLock();
	}

	/**
	 * Schedule a job to be done at the given time.
	 * 
	 * @param execTime
	 *            the scheduled time to execute the job. This time complies with
	 *            {@code System.currentTimeMillis()} (wall clock time in UTC).
	 *            Note that the job is triggered if this execTime is smaller
	 *            than the current system time.
	 * @param job
	 *            the job to execute.
	 * @return a string that uniquely identifies this job. This string can be
	 *         used later to cancel this job.
	 */
	public String schedule(long execTime, Runnable job){
		shutdownLock.lock();
		if (shutDown) {
			shutdownLock.unlock();
			throw new TimedExecutorShutDownException();
		}
		String jobId = UUID.randomUUID().toString();
		Intent intent = new Intent(jobId);
		IntentFilter filter = new IntentFilter(jobId);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
				0);
		Receiver receiver = new Receiver(job);

		context.registerReceiver(receiver, filter);
		alarmManager.set(AlarmManager.RTC_WAKEUP, execTime, pending);
		// update pending jobs
		TimedExecutorJob executorJob = new TimedExecutorJob(execTime, jobId,
				pending, receiver);
		pendingJobs.put(jobId, executorJob);
		pendingTimes.add(executorJob);
		shutdownLock.unlock();
		return jobId;
	}

	/**
	 * Whether the given job is still pending.
	 * 
	 * @param jobId
	 *            the unique identifier of the job.
	 * @return
	 */
	public boolean hasPendingJob(String jobId) {
		return pendingJobs.containsKey(jobId);
	}

	/**
	 * Cancel a previously scheduled job.
	 * 
	 * @param jobId
	 */
	public void cancel(String jobId) {
		TimedExecutorJob job = pendingJobs.remove(jobId);
		if (job != null) {
			pendingTimes.remove(job);
			alarmManager.cancel(job.intent);
			try {
				context.unregisterReceiver(job.receiver);
			} catch (IllegalArgumentException e) {
				Log.i(TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * Terminate this executor and cancel all the currently scheduled jobs. Note
	 * that this executor can be re-used to reschedule jobs.
	 * 
	 */
	public void cancelAllPendingJobs() {
		// Cancel all the pending jobs.
		Iterator<Entry<String, TimedExecutorJob>> it = pendingJobs.entrySet()
				.iterator();
		TimedExecutorJob job;
		Entry<String, TimedExecutorJob> entry;
		while (it.hasNext()) {
			entry = it.next();
			job = entry.getValue();
			if (job != null) {
				alarmManager.cancel(job.intent);
				try {
					context.unregisterReceiver(job.receiver);
				} catch (IllegalArgumentException e) {
					Log.i(TAG, e.getMessage(), e);
				}
			}
		}
		pendingJobs.clear();
		pendingTimes.clear();
	}

	/**
	 * Shut down the executor. Cancel all pending jobs and wait for currently
	 * executing jobs to finish before returning.
	 * 
	 */
	public void shutDown(){
		// Cancel all pending jobs
		shutdownLock.lock();
		if (shutDown) {
			shutdownLock.unlock();
			throw new TimedExecutorShutDownException();
		}
		shutDown = true;
		shutdownLock.unlock();
		cancelAllPendingJobs();
		// Wait for all current jobs to finish
		ArrayList<Thread> threads = new ArrayList<Thread>();
		runningThreads.drainTo(threads);
		Iterator<Thread> it = threads.iterator();
		while (it.hasNext()) {
			try {
				it.next().join();
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	/**
	 * Return the execution time of the job in the nearest future.
	 * 
	 * @return
	 */
	public Long getNextExecTime() {
		TimedExecutorJob jobNext = pendingTimes.peek();
		if (jobNext != null)
			return jobNext.execTime;
		else
			return null;
	}

	/**
	 * Return the jobId of the job in the nearest future.
	 * 
	 * @return
	 */
	public String getNextPendingJob() {
		TimedExecutorJob jobNext = pendingTimes.peek();
		if (jobNext != null)
			return jobNext.jobId;
		else
			return null;
	}

	/**
	 * {@code BroadcastReceiver} subclass that is invoked by the AlarmManger.
	 * 
	 * @author Yue Liu
	 * 
	 */
	private class Receiver extends BroadcastReceiver {

		private final Runnable runnable;
		private final WakeLock wakeLock;

		public Receiver(Runnable runnable) {
			this.runnable = runnable;

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
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
			shutdownLock.lock();
			if (shutDown) {
				shutdownLock.unlock();
				wakeLock.release();
				return;
			}
			String jobId = intent.getAction();
			// clear up
			TimedExecutorJob job = pendingJobs.remove(jobId);
			if (job != null)
				pendingTimes.remove(job);
			try {
				context.unregisterReceiver(this);
			} catch (IllegalArgumentException e) {
				Log.i(TAG, e.getMessage(), e);
			}
			Thread thread = new SelfReferencingThread(runnable);
			try {
				runningThreads.add(thread);
				thread.start();
			} catch (IllegalStateException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			shutdownLock.unlock();
		}

		/**
		 * We extends {@link Thread} with this class so that it can refer to
		 * itself during {@code run()} call.
		 * 
		 * @author Yue Liu
		 * 
		 */
		private class SelfReferencingThread extends Thread {
			private Runnable runnable;

			public SelfReferencingThread(Runnable runnable) {
				this.runnable = runnable;
			}

			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
					runningThreads.remove(this);
					wakeLock.release();
				}
			}
		}

	}

	/**
	 * This class describes each job in {@link TimedExecutor}.
	 * 
	 * @author Yue Liu
	 * 
	 */
	public static class TimedExecutorJob implements
			Comparable<TimedExecutorJob> {
		public final Long execTime;
		public final String jobId;
		public final PendingIntent intent;
		public final Receiver receiver;

		public TimedExecutorJob(Long execTime, String jobId,
				PendingIntent intent, Receiver receiver) {
			this.execTime = execTime;
			this.jobId = jobId;
			this.intent = intent;
			this.receiver = receiver;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj.getClass() != this.getClass()) {
				return false;
			}
			if (((TimedExecutorJob) obj).execTime - execTime == 0
					&& ((TimedExecutorJob) obj).jobId.compareTo(jobId) == 0) {
				return true;
			} else
				return false;
		}

		@Override
		public int hashCode() {
			int hashCode = 0;
			for (int i = 0; i < jobId.length(); i++) {
				hashCode += jobId.charAt(i);
			}
			hashCode = hashCode * 10;
			hashCode += execTime.intValue();
			return hashCode;
		}

		@Override
		public int compareTo(TimedExecutorJob arg0) {
			return (int) (this.execTime - arg0.execTime);
		}
	}
}
