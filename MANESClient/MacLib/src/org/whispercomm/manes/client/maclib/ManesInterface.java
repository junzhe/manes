package org.whispercomm.manes.client.maclib;

import java.util.concurrent.TimeUnit;

import org.whispercomm.manes.client.ErrorCode;
import org.whispercomm.manes.client.RemoteMac;
import org.whispercomm.manes.client.macentity.network.ManesService;
import org.whispercomm.manes.client.maclib.util.AwaitableServiceConnection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class represents an interface for sending and receiving one-hop
 * broadcast packets via the MANES ad hoc network emulation system.
 * <p>
 * <code>ManesInterface</code> depends on
 * {@link org.whispercomm.manes.client.macentity.network.ManesService
 * ManesService}, which must be installed.
 * 
 * @see org.whispercomm.manes.client.macentity.network.ManesService ManesService
 * 
 * @author Yue Liu
 * @author David R. Bild
 */
public class ManesInterface {
	private static final String TAG = ManesInterface.class.getSimpleName();

	private static final String SERVICE_INTENT_ACTION = "org.whispercomm.manes.client.service";

	/**
	 * The maximum allowable size in bytes of an outgoing packet.
	 */
	public static final int MANES_MTU = 2346; // the same as 802.11

	/**
	 * The context of the caller application
	 */
	private Context context;

	/**
	 * The ID of the application using the MANES Interface
	 */
	private final int appId;

	/**
	 * Callback to invoke when the service connection is established or dropped.
	 */
	private final ManesConnection clientConn;

	/*
	 * The remote services and associated connections
	 */
	private volatile RemoteMac remoteMac;
	private AwaitableServiceConnection remoteMacConnection;

	/**
	 * Constructs a new <code>ManesInterface</code> object to send and receive
	 * packets with the specified application id.
	 * <p>
	 * The application id is used to filter incoming packets to the appropriate
	 * application and thus must consistent across all instances of the
	 * application and globally unique to the application. To ensure uniqueness,
	 * it should be registered in the <a href=
	 * "http://whispercomm.org/manes/assignments/protocol-numbers/protocol-numbers.xml"
	 * >MANES application id registry</a>. Application IDs 253, 254, and 255 are
	 * available for testing and experimentation.
	 * <p>
	 * The constructor binds the specified context to the
	 * {@link org.whispercomm.manes.client.macentity.network.ManesService
	 * ManesService} background service. However, a call to
	 * {@link ManesInterface#initialize() initialize} must be made after
	 * constructing ManesInterface, in order to allocate storage. When finished,
	 * the {@code ManesInterface} instance must be unbound from the service by
	 * calling {@link ManesInterface#disconnect() disconnect}.
	 * 
	 * @param appId
	 *            the registered identifier for this application.
	 * @param context
	 *            the context used to bind to {@code ManesService}.
	 * @param conn
	 *            callbacks to invoke when the service connection is established
	 *            or lost.
	 * @throws ManesNotInstalledException
	 *             if the MANES client application is not installed.
	 * @throws ManesIllegalAppIdException
	 *             if an illegal (i.e., negative) application id is supplied.
	 */
	public ManesInterface(int appId, Context context, ManesConnection conn)
			throws ManesNotInstalledException, ManesIllegalAppIdException {
		if (appId < 0)
			throw new ManesIllegalAppIdException();

		this.appId = appId;
		this.context = context;
		this.clientConn = conn;

		// Initiate service connections;
		remoteMacConnection = new AwaitableServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "Connected to service.");
				remoteMac = RemoteMac.Stub.asInterface(service);
				ManesInterface.this.initiate();
				if (clientConn != null) {
					clientConn.onManesServiceConnected();
				}
				super.onServiceConnected(name, service);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.w(TAG, "Disconnected from service unexpectedly.");
				super.onServiceDisconnected(name);
				if (clientConn != null) {
					clientConn.onManesServiceDisconnected();
				}
			}
		};

		// Bind to the remote service using an intent
		Log.i(TAG, "Binding to ManesService.");
		if (!context.bindService(new Intent(SERVICE_INTENT_ACTION),
				remoteMacConnection, Context.BIND_AUTO_CREATE)) {
			Log.e(TAG, "Bind failed.");
			throw new ManesNotInstalledException();
		}
	}

	private void initiate() {
		try {
			remoteMac.initiate(appId);
		} catch (RemoteException e) {
			// Should only happen if the remote service has died, in which case
			// OnServiceConnected() will be called again when Android restarts
			// the it and initiate() will be called again. So, just
			// ignore the exception.
			Log.e(TAG, "initiate() failed.", e);
		}
	}

	public boolean registered() {
		if (remoteMac == null) {
			Log.w(TAG, "registered() failed. RemoteMac not bound.");
			return false;
		}

		try {
			return remoteMac.registered();
		} catch (RemoteException e) {
			Log.e(TAG, "registered() failed.", e);
			return false;
		}
	}

	/**
	 * Broadcasts the given packet to this device's one-hop neighborhood.
	 * <p>
	 * The packet size must not exceed {@link #MANES_MTU}.
	 * 
	 * @param data
	 *            the packet to be sent.
	 * @return {@code true} if the packet as delivered to the network stack,
	 *         {@code false} otherwise.
	 * @throws ManesNotRegisteredException
	 *             if the client is not registered with MANES server.
	 * @throws ManesFrameTooLargeException
	 *             if the packet size exceeds {@code MANES_MTU}.
	 */
	public boolean send(byte[] data) throws ManesNotRegisteredException,
			ManesFrameTooLargeException {
		if (data.length > MANES_MTU) {
			throw new ManesFrameTooLargeException(String.format(
					"Maximum packet size is %d.  Got %d.", MANES_MTU,
					data.length));
		}

		if (remoteMac == null) {
			Log.w(TAG, "send() failed. RemoteMac not bound.");
			return false;
		}

		try {
			ErrorCode rc = remoteMac.send(appId, data);
			switch (rc) {
			case SUCCESS:
				return true;
			case NOT_REGISTERED:
				throw new ManesNotRegisteredException();
			default:
				Log.e(TAG, "send() failed with return code: " + rc.name());
				return false;
			}
		} catch (RemoteException e) {
			Log.e(TAG, "send() failed.", e);
			return false;
		}
	}

	/**
	 * Retrieves an incoming packet, blocking until one is available, up to the
	 * specified timeout.
	 * 
	 * @param timeout
	 *            the maximum time in milliseconds to block waiting for an
	 *            incoming packet.
	 * @return the received packet; {@code null} on timeout.
	 */
	public byte[] receive(long timeout) {
		try {
			if (remoteMacConnection.await(timeout, TimeUnit.MILLISECONDS)) {
				return remoteMac.receive(appId, timeout);
			}
		} catch (InterruptedException e) {
		} catch (RemoteException e) {
			Log.e(TAG, "receive() failed.", e);
		}
		return null;
	}

	/**
	 * Closes the interface, unbinds from {@link ManesService}.
	 * <p>
	 * The method must be called when an application is finished with the
	 * interface, e.g., before exiting.
	 */
	public void disconnect() {
		if (remoteMac != null) {
			try {
				remoteMac.disconnect(appId);
			} catch (RemoteException e) {
				// Should only happen if the remote service failed, so no need
				// to disconnect anyway.
				Log.w(TAG, "disconnect() failed.", e);
			}
			context.unbindService(remoteMacConnection);
			remoteMac = null;
		}
	}

	/**
	 * Unregisters from
	 * {@link org.whispercomm.manes.client.macentity.network.ManesService
	 * ManesService} if the application failed to call
	 * {@link ManesInterface#unregister unregister}.
	 * <p>
	 * This is a partial safeguard against faulty applications, but should not
	 * be relied upon. {@code finalize} is not guaranteed to be called.
	 */
	@Override
	protected void finalize() throws Throwable {
		// Notify ManesService to unregister itself
		disconnect();
		super.finalize();
	}

	/**
	 * Callbacks to notify the owner of a ManesInterface instance when the
	 * connection to ManesService is made or lost.
	 * <p>
	 * Calls to certain methods (e.g., {@link ManesInterface#send(byte[])}) may
	 * fail or throw exceptions when the service is not connected.
	 * <p>
	 * Will be executed on the main thread.
	 * 
	 * @author David R. Bild
	 * 
	 */
	public interface ManesConnection {
		/**
		 * Called when the service is connected.
		 */
		public void onManesServiceConnected();

		/**
		 * Called when the service is disconnected.
		 */
		public void onManesServiceDisconnected();
	}

}
