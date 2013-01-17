package org.whispercomm.manes.exp.locationsensor.location.sensor;

import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.Wifi;
import org.whispercomm.manes.exp.locationsensor.data.WifiDirectGroup;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.util.PeriodicExecutor;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

@SuppressLint("SdCardPath")
public class WifiDirectGroupCreator extends BroadcastReceiver implements
		LocationSensor<WifiDirectGroup>, WifiP2pManager.ChannelListener,
		WifiP2pManager.ActionListener, WifiP2pManager.GroupInfoListener {

	public static final String TAG = WifiDirectGroupCreator.class
			.getSimpleName();
	public static final long GROUP_CREATE_PERIOD = 1 * 60 * 1000;

	private WifiP2pManager wifiDirectManager;
	private WifiP2pManager.Channel wifiDirectChannel;
	private boolean isSensing;
	private PeriodicExecutor executor;
	private GroupCreator groupCreator;
	private GeneralOperator operator;
	private WifiDirectGroup wifiDirectCrt;
	private IntentFilter intentFilter;
	private Context context;

	public WifiDirectGroupCreator(Context context, Looper looper) {
		this.context = context;
		this.wifiDirectManager = (WifiP2pManager) context
				.getSystemService(Context.WIFI_P2P_SERVICE);
		this.wifiDirectChannel = wifiDirectManager.initialize(context, looper,
				this);
		this.isSensing = false;
		this.groupCreator = new GroupCreator();
		this.executor = new PeriodicExecutor(context, TAG + ".groupcreator",
				groupCreator);
		this.wifiDirectCrt = null;
		this.intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}

	@Override
	synchronized public void startSensing() {
		if (isSensing == false) {
			isSensing = true;
			context.registerReceiver(this, intentFilter);
			executor.start(GROUP_CREATE_PERIOD, true);
		}
	}

	@Override
	synchronized public void stopSensing() {
		if (isSensing) {
			isSensing = false;
			executor.stop();
			context.unregisterReceiver(this);
			removeWifiDirectGroup();
		}
	}

	@Override
	public void startPeriodicMeasures(long peirod) {
		// do nothing here.
	}

	@Override
	public void updateReadings(WifiDirectGroup newReadings) {
		wifiDirectCrt = newReadings;
	}

	@Override
	public WifiDirectGroup getLatestReading() {
		return wifiDirectCrt;
	}

	@Override
	public boolean isSensing() {
		return isSensing;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = (GeneralOperator) operator;
	}

	synchronized private void createWifiDirectGroup() {
		if (isSensing) {
			Log.i(TAG, "***Creating Wifi group");
			wifiDirectManager.createGroup(wifiDirectChannel, this);
		}
	}

	private void removeWifiDirectGroup() {
		// do not need to remove Wifi group because they automatically disappear
		// according to our experiments.
		// Log.i(TAG, "***Removed Wifi group!");
		// wifiDirectManager.removeGroup(wifiDirectChannel, this);
		// isWaitingForGroupInfo = false;
	}

	private class GroupCreator implements Runnable {

		@Override
		public void run() {
			createWifiDirectGroup();
		}

	}

	@Override
	public void onChannelDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailure(int reason) {
		Log.e(TAG, "***********Create group fails*********: " + reason);
		// if (isSensing) {
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(10 * 1000);
		// } catch (InterruptedException e) {
		// // do nothing here
		// }
		// wifiDirectManager.createGroup(wifiDirectChannel,
		// WifiDirectGroupCreator.this);
		// }
		//
		// }).start();
		// }
	}

	@Override
	public void onSuccess() {
		Log.e(TAG, "Create group succeeds.");
		wifiDirectManager.requestGroupInfo(wifiDirectChannel, this);
	}

	@Override
	public void onGroupInfoAvailable(WifiP2pGroup group) {
		if (isSensing == false)
			return;
		Log.i(TAG, "***wifi direct group info available!");
		if (group == null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						// do nothing here
					}
					wifiDirectManager.requestGroupInfo(wifiDirectChannel,
							WifiDirectGroupCreator.this);
				}

			}).start();
		} else {
			Log.i(TAG, "***wifi direct group info is not null!");
			WifiDirectGroup info = new WifiDirectGroup();
			info.setTime(HumanReadableTime.getCurrentTime());
			String netif = group.getOwner().deviceAddress;
			Log.i(TAG, "***net interface: " + netif);
			info.setNetInterface(netif);
			info.setNetifLong(Wifi.TranslateMacToLong(netif));
			info.setSsid(group.getNetworkName());
			updateReadings(info);
			operator.inform(SensorSignal.WIFI_DIRECT_AVAILABLE);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			Log.i(TAG, "*******wifi_p2p_state_changed***********");
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			Log.i(TAG, "********wifi_p2p_peers_changed************");
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			// Respond to new connection or disconnections
			Log.i(TAG, "***********wifi_p2p_connection_changed***********");
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			// Respond to this device's wifi state changing
			Log.i(TAG, "**********wifi_p2p_this_device_changed***********");
		}
	}
}
