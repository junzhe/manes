package org.whispercomm.manes.client.macentity.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;

import static org.mockito.Mockito.mock;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TimedExecutorTest {

	private TimedExecutor executor;

	@Before
	public void setUp() {
		Context context = new Activity();
		executor = new TimedExecutor(context);
	}

	@Test
	public void testCancel() {
		// mock AlarmManager
		AlarmManager alarmManager = mock(AlarmManager.class);
		Whitebox.setInternalState(executor, AlarmManager.class, alarmManager);
		Runnable job = new Runnable() {
			@Override
			public void run() {
			}
		};
		long sleepTime = 10;
		long crtTime = System.currentTimeMillis();
		String job1 = executor.schedule(crtTime + sleepTime, job);
		executor.cancel(job1);
		assertFalse(executor.hasPendingJob(job1));
	}

	@Test
	public void testCancelAll() {
		// mock AlarmManager
		AlarmManager alarmManager = mock(AlarmManager.class);
		Whitebox.setInternalState(executor, AlarmManager.class, alarmManager);
		Runnable job = new Runnable() {
			@Override
			public void run() {
			}
		};
		long sleepTime = 10;
		long crtTime = System.currentTimeMillis();
		String job1 = executor.schedule(crtTime + sleepTime, job);
		String job2 = executor.schedule(crtTime + 2 * sleepTime, job);
		String job3 = executor.schedule(crtTime + 3 * sleepTime, job);
		executor.cancelAllPendingJobs();
		assertTrue(executor.getNextExecTime() == null);
		assertFalse(executor.hasPendingJob(job1));
		assertFalse(executor.hasPendingJob(job2));
		assertFalse(executor.hasPendingJob(job3));
		job1 = executor.schedule(crtTime + sleepTime, job);
		assertTrue(executor.hasPendingJob(job1));
	}

	@Test
	public void testGetNextExecTime() {
		// mock AlarmManager
		AlarmManager alarmManager = mock(AlarmManager.class);
		Whitebox.setInternalState(executor, AlarmManager.class, alarmManager);
		Runnable job = new Runnable() {
			@Override
			public void run() {
			}
		};
		long sleepTime = 10;
		long crtTime = System.currentTimeMillis();
		String job2 = executor.schedule(crtTime + 2 * sleepTime, job);
		String job3 = executor.schedule(crtTime + 3 * sleepTime, job);
		executor.cancel(executor.getNextPendingJob());
		assertFalse(executor.hasPendingJob(job2));
		assertTrue(executor.hasPendingJob(job3));
		assertTrue(executor.getNextExecTime() - (crtTime + 3 * sleepTime) == 0);
		executor.cancel(executor.getNextPendingJob());
		assertFalse(executor.hasPendingJob(job3));
		assertTrue(executor.getNextExecTime() == null);
	}

}
