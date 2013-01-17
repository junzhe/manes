package org.whispercomm.manes.client.macentity.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.whispercomm.manes.client.ErrorCode;
import org.whispercomm.manes.client.RemoteMac;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.location.ManesLocationManager;

/**
 * This is a shared background service that functions as a virtual wireless
 * interface. It sends and receives messages via the MANES ad-hoc network
 * emulation system.
 * <p>
 * The service has two primary tasks. First, it routes packets between
 * applications and the MANES server. Second, it collects and reports the data
 * used for topology estimation.
 * <p>
 * Applications should access the service via
 * {@link org.whispercomm.manes.client.maclib.ManesInterface ManesInterface}.
 * Also, the following lines must be added to the application's manifest file. <br />
 * <code>
 * &lt;service<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;android:enabled="true"<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;android:name="org.whispercomm.manes.client.macentity.network.ManesService"<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;android:exported="true"<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;android:process=":remote"/>
 * </code>
 * 
 * @see org.whispercomm.manes.client.maclib.ManesInterface ManesInterface
 * @author David Adrian
 * @author Yue Liu
 */
public class ManesService extends Service {

	private static final String TAG = ManesService.class.getName();
	public static final String SERVER_ADDRESS = "packet.api.manes.whispercomm.org";
	public static final String SERVER_URL = "http://" + SERVER_ADDRESS
			+ ":7889";
	public static final String APP_ID_INTENT_KEY = "app_id";
	private PacketManager packetManager;
	private HttpManager httpManager;
	private IdManager idManager;
	private UdpPacketListener udpListener;
	private KeepaliveSender keepaliveSender;
	private ManesLocationManager locationManager;
	private NetworkConnectivityMonitor networkConnMonitor;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate() called.  Starting ManesService.");
		this.httpManager = new HttpManager();
		this.idManager = new IdManager(this.getApplicationContext());
		this.packetManager = new PacketManager(httpManager, idManager,
				this.getApplicationContext());
		this.udpListener = new UdpPacketListener(this, packetManager, idManager);
		this.udpListener.start();
		this.keepaliveSender = new KeepaliveSender(this, udpListener);
		keepaliveSender.start(UdpPacketListener.PERIOD_MS);
		this.locationManager = new ManesLocationManager(httpManager, idManager,
				this);
		locationManager.start();
		this.networkConnMonitor = new NetworkConnectivityMonitor(this,
				keepaliveSender, locationManager);
		keepaliveSender.setFailureHandler(networkConnMonitor);
		Log.i(TAG, "Started.");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy() called.  Stopping ManesService.");
		if (locationManager != null) {
			locationManager.stop();
		}
		if (keepaliveSender != null)
			keepaliveSender.stop();
		if (udpListener != null) {
			udpListener.stop();
		}
		httpManager.shutdown();
		Log.i(TAG, "Stopped.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind() called.");
		return manesBinder;
	}

	/**
	 * The actual implementation of the RemoteMac API defined in AIDL
	 */
	private final RemoteMac.Stub manesBinder = new RemoteMac.Stub() {

		@Override
		public void initiate(int appId) throws RemoteException {
			Log.i(TAG, String.format("initiate(appId=%d) called.", appId));
			packetManager.addQueueUser(appId);
		}

		@Override
		public byte[] receive(int appId, long timeout) throws RemoteException {
			byte[] packet = null;
			try {
				packet = packetManager.receive(appId, timeout);
			} catch (InterruptedException e) {
				Log.w(TAG, "Interrupted while receiving packet.", e);
			}
			return packet;
		}

		@Override
		public ErrorCode send(int appId, byte[] contents)
				throws RemoteException {
			try {
				packetManager.send(appId, contents);
				return ErrorCode.SUCCESS;
			} catch (NotRegisteredException e) {
				Log.e(TAG, "Failed to send packet.", e);
				return ErrorCode.NOT_REGISTERED;
			}
		}

		@Override
		public void disconnect(int appId) throws RemoteException {
			Log.i(TAG, String.format("disconnect(appId=%d) called.", appId));
			packetManager.removeQueueUser(appId);
		}

		@Override
		public boolean registered() throws RemoteException {
			return idManager.isRegistered();
		}
	};
}
