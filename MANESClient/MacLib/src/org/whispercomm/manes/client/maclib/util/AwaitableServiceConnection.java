package org.whispercomm.manes.client.maclib.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * This class extends {@code ServiceConnection} to allow a calling thread to
 * wait, using the {@link #await()} and {@link #await(long, TimeUnit)} methods,
 * for the service to be connected.
 * <p>
 * Subclasses overriding the {@link #onServiceConnected(ComponentName, IBinder)}
 * and {@link #onServiceDisconnected(ComponentName)} methods must call through
 * to this class via {@code super.onServiceConnected(ComponentName, IBinder)}
 * and {@code super.onServiceDisconnected(ComponentName)} <i>after</i>
 * performing any post-connection setup.
 * 
 * @author David R. Bild
 * 
 */
public abstract class AwaitableServiceConnection implements ServiceConnection {

	private volatile CountDownLatch latch;

	public AwaitableServiceConnection() {
		latch = new CountDownLatch(1);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		latch.countDown();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		latch = new CountDownLatch(1);
	}

	/**
	 * Causes the current thread to wait until the service is connected, unless
	 * the thread is interrupted.
	 * <p>
	 * If the service is already connected then this method returns immediately.
	 * <p>
	 * The service could disconnect after this method returns, but subsequent
	 * calls will wait until the service is reconnected.
	 * 
	 * @throws InterruptedException
	 *             if the current thread is interrupted while waiting
	 */
	public void await() throws InterruptedException {
		latch.await();
	}

	/**
	 * Causes the current thread to wait until the service is connected, unless
	 * the thread is interrupted, or the specified waiting time elapses.
	 * <p>
	 * If the service is already connected then this method return immediately.
	 * <p>
	 * The service could disconnect after this method returns, but subsequent
	 * calls will wait until the service is reconnected.
	 * 
	 * @param timeout
	 *            the maximum time to wait
	 * @param unit
	 *            the time unit of the {@code timeout} argument
	 * @return {@code true} if the service was connected and {@code false} if
	 *         the waiting time elapsed before it connected.
	 * @throws InterruptedException
	 */
	public boolean await(long timeout, TimeUnit unit)
			throws InterruptedException {
		return latch.await(timeout, unit);
	}
}
