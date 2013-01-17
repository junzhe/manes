package org.whispercomm.manes.exp.testconnectivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final String TAG = "******testconnectivity.mainactivity******";
	static final long MEASURE_PERIOD = 21 * 1000;// In each round, periodic
													// measurement every 21s.
	static final long SYNC_DELAY = 3 * 60 * 1000;// Wait for 5min for the
													// operator to synchronize
													// and put the device in
													// place.
	static final int MEASURE_NUM = 15;// Number of measurements each round.
	static final long SCAN_DURATION = 10 * 1000;// Duration of a WIFI scan.
	public static final String STORAGE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/connectivity";

	protected static final int PORT = 62638; // "manes" on cellphone keyboard +
												// 1

	static final int TEXTVIEW_HEIGHT_IN_LINE_COUNT = 5; // the height of the
														// textviews in line
														// count

	LinearLayout ll;
	Button startAsSender;
	Button startAsReceiver;
	Button sync;
	Button start;
	TextView instructionView;
	TextView infoView;
	EditText editLocationText;
	UiHandler uiHandler;

	// synchronize helper
	SyncHelper syncHelper;

	// network related variables
	WifiManager wm;
	MulticastSocket ms; // multi-cast socket
	InetAddress group; // multicast group address

	// result files
	FileOutputStream scanRecordFos;
	FileOutputStream probeResultFos;

	/** synchronized start time */
	Long startTime;
	/** index of measurement within on round */
	int scanIndex;
	/** location index of current measurement round */
	String location;

	/** scheduler to perform the measurements */
	Timer measureScheduler;

	/** sender (0) or receiver (-1) role */
	int role;
	static final int SENDER = 0;
	static final int RECEIVER = 1;
	/** whether the probe message is received by the receiver */
	ProbResult probeResult;

	@Override
	public void onDestroy() {
		syncHelper.isAppRunning = false;
		try {
			scanRecordFos.close();
			probeResultFos.close();
		} catch (IOException e) {
			Log.i(TAG, e.getMessage());
		}
		ms.close();
		measureScheduler.cancel();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (initializeNetwork() == -1)
			return;
		initializeGUI();
		uiHandler = new UiHandler();
		syncHelper = new SyncHelper(uiHandler, start, sync, instructionView,
				ms, group);
		wm = (WifiManager) getSystemService(WIFI_SERVICE);
		measureScheduler = new Timer();
		probeResult = new ProbResult();
		// get the file that records the AP scan results.
		try {
			scanRecordFos = Ultility.openFileStream(STORAGE_PATH,
					"scanRecord.dat");
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
			instructionView.append("Fail to open scanRecord file. "
					+ e.getMessage());
			return;
		}
		probeResultFos = null;
		// register broadcast receiver to get scan result
		registerReceiver(new WifiReceiver(), new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		// initialize listeners
		initializeSender();
		initializeReceiver();
		initializeSynchronizer();
		initializeRoundStarter();
	}

	/**
	 * 
	 * Broadcast receiver for receiving wifi scan result. It stores the scan
	 * result.
	 * 
	 */
	class WifiReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// get the scan result into a string
			String scanResult = "";
			List<ScanResult> scan = wm.getScanResults();
			for (ScanResult result : scan) {
				String name = result.SSID;
				String address = result.BSSID;
				String capacity = result.capabilities;
				int frequency = result.frequency;
				int level = result.level;
				scanResult = scanResult + name + " " + address + " " + capacity
						+ " " + frequency + " " + level + "\n";
			}
			long crtTime = System.currentTimeMillis() - syncHelper.startTime;
			// ***always set the result to be -1
			// The receiver would have real scan result recorded in another
			// file, which we can merge in data processing
			int probeRslt = -1;
			scanResult = "\ntime: " + crtTime + "\nlocation: " + location
					+ "\n" + "scanIndex: " + scanIndex + "\n" + "probRslt: "
					+ probeRslt + "\n" + scanResult;
			// open the "scanrecord" file
			try {
				scanRecordFos.write(scanResult.getBytes());
				scanRecordFos.write(new String("\n").getBytes());
				scanRecordFos.flush();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				instructionView.append("Fail to record scan result. "
						+ e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				instructionView.append("Fail to record scan result. "
						+ e.getMessage());
			}
			// update the scan result to screen
			// ***not sure whether this works since no handler is used
			infoView.setText(scanResult);
		}

	}

	private void initializeGUI() {
		ll = new LinearLayout(this);
		ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		ll.setOrientation(LinearLayout.VERTICAL);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		instructionView = new TextView(this);
		instructionView.setHeight(TEXTVIEW_HEIGHT_IN_LINE_COUNT
				* instructionView.getLineHeight());
		instructionView
				.setMovementMethod(ScrollingMovementMethod.getInstance());
		ll.addView(instructionView);
		setContentView(ll);
		instructionView.append("Please choose role.\n");

		startAsSender = new Button(this);
		startAsSender.setText("Start As Sender");
		ll.addView(startAsSender);

		startAsReceiver = new Button(this);
		startAsReceiver.setText("Start As Receiver");
		ll.addView(startAsReceiver);

		sync = new Button(this);
		sync.setText("Start synchronization");
		ll.addView(sync);
		sync.setEnabled(false);

		start = new Button(this);
		start.setText("Start one measurement round");
		ll.addView(start);
		start.setEnabled(false);

		editLocationText = new EditText(this);
		ll.addView(editLocationText);

		infoView = new TextView(this);
		infoView.setHeight(TEXTVIEW_HEIGHT_IN_LINE_COUNT
				* instructionView.getLineHeight());
		infoView.setMovementMethod(ScrollingMovementMethod.getInstance());
		ll.addView(infoView);
		setContentView(ll);
	}

	// open a multi-cast socket and join the corresponding group
	private int initializeNetwork() {

		InetAddress deviceAddr;
		// get group address
		try {
			group = InetAddress.getByName("224.0.0.111");
		} catch (UnknownHostException e1) {
			Log.e(TAG, "unknown host");
			return -1;
		}

		// bind to socket
		try {
			deviceAddr = InetAddress.getByName(Ultility.getWifiIp(this));
			NetworkInterface iface = NetworkInterface
					.getByInetAddress(deviceAddr);

			ms = new MulticastSocket(PORT);
			ms.setNetworkInterface(iface);
			ms.setInterface(deviceAddr);
			ms.joinGroup(group);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.toString());
			return -1;
		} catch (SocketException e) {
			Log.e(TAG, e.toString());
			return -1;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return -1;
		}

		return 1;
	}

	/**
	 * initialize listener for startAsSender
	 */
	void initializeSender() {
		startAsSender.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				instructionView.append("I am the sender.");
				role = SENDER;
				startAsSender.setEnabled(false);
				startAsReceiver.setEnabled(false);
				sync.setEnabled(true);
			}

		});
	}

	/**
	 * initialize listener for startAsReciver
	 */
	void initializeReceiver() {
		startAsReceiver.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				instructionView.append("I am the receiver.");
				role = RECEIVER;
				startAsSender.setEnabled(false);
				startAsReceiver.setEnabled(false);
				// initiate the noiseRecord file if it is not opened yet.
				try {
					probeResultFos = Ultility.openFileStream(STORAGE_PATH,
							"probeResult.dat");
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage());
					instructionView.append("Fail to open probeResult file. "
							+ e.getMessage());
				}
				// start the receiver thread
				new Thread(new Receiver(syncHelper, probeResult)).start();
			}
		});
	}

	/**
	 * initialize listener for sync
	 */
	void initializeSynchronizer() {
		sync.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sync.setEnabled(false);
				instructionView.append("Synchronization starts...");
				// start synchronizer thread
				new Thread(new Synchronizer(syncHelper)).start();
			}

		});
	}

	/**
	 * initialize listener for start
	 */
	void initializeRoundStarter() {
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Check whether location is entered
				location = editLocationText.getText().toString();
				if (location.compareToIgnoreCase("") == 0) {
					instructionView
							.append("Enter the current location index before starting measurement.");
					return;
				}
				instructionView.append("Starting a new measurement round in "
						+ (System.currentTimeMillis() - syncHelper.startTime)
						/ 1000 + " seconds.");
				// clear up editLocationText
				editLocationText.setText("");
				// disable start button
				start.setEnabled(false);
				// initialization
				scanIndex = 0;
				// Schedule measurement event
				scheduleMeasures();
			}
		});
	}

	/**
	 * Schedule a sequence of measure events according to the role.
	 */
	void scheduleMeasures() {
		long startTime;
		long crtTime;
		int i;
		switch (role) {
		case SENDER:
			startTime = syncHelper.startTime + SYNC_DELAY + SCAN_DURATION;
			for (i = 0; i < MEASURE_NUM; i++) {
				crtTime = startTime + i * MEASURE_PERIOD;
				measureScheduler.schedule(new OneSenderMeasure(), new Date(
						crtTime));
			}
			// Schedule to go back to the state ready for synchronization
			crtTime = startTime + i * MEASURE_PERIOD;
			measureScheduler.schedule(new TimerTask() {
				@Override
				public void run() {
					uiHandler.enableButton(sync, true);

				}
			}, new Date(crtTime));
			break;
		case RECEIVER:
			startTime = syncHelper.startTime + SYNC_DELAY;
			for (i = 0; i < MEASURE_NUM; i++) {
				crtTime = startTime + i * MEASURE_PERIOD;
				measureScheduler.schedule(new OneReceiverMeasure(), new Date(
						crtTime));
			}
			break;
		}

	}

	/**
	 * 
	 * Keeps probe result across multiple threads.
	 * 
	 */
	protected static class ProbResult {
		public int result;

		public ProbResult() {
			this.result = 0;
		}
	}

	/**
	 * 
	 * One measurement on the sender's side.
	 * 
	 */
	class OneSenderMeasure extends TimerTask {

		@Override
		public void run() {
			// update the scanIndex
			scanIndex += 1;
			// send the probe message
			byte[] probeBytes;
			try {
				// ***not sure whether this scanIndex will change
				probeBytes = new Message(Message.PROBE).toBytes();
				DatagramPacket syncPck = new DatagramPacket(probeBytes,
						probeBytes.length, group, PORT);
				syncHelper.ms.send(syncPck);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				instructionView.append("Fail to send out probe. "
						+ e.getMessage());
				return;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				instructionView.append("Fail to send out probe. "
						+ e.getMessage());
				return;
			}
			// after 1s, scan the network and get the result
			measureScheduler.schedule(new TimerTask() {

				@Override
				public void run() {
					wm.startScan();
				}

			}, 1000);
		}

	}

	/**
	 * 
	 * One measurement on the receiver's side
	 * 
	 */
	class OneReceiverMeasure extends TimerTask {

		@Override
		public void run() {
			// update the scanIndex
			scanIndex += 1;
			// scan the network and get the result
			wm.startScan();
			// after 11s
			measureScheduler.schedule(new TimerTask() {

				@Override
				public void run() {
					long crtTime = System.currentTimeMillis()
							- syncHelper.startTime;
					// sample "/proc/net/wiress" file to get the noise
					try {
						Ultility.recordProcWireless(crtTime, location);
					} catch (IOException e1) {
						Log.e(TAG, e1.getMessage());
						uiHandler.appendToTextView(instructionView,
								"Fail to write from /proc/net/wireless"
										+ " to noiseRecord" + e1.getMessage());
						//***how to handle error here?
					}
					// record the probe result
					String probeResultString = "time: " + crtTime
							+ "\nlocation: " + location + "\n" + "probRslt: "
							+ probeResult.result + "\n";
					try {
						probeResultFos.write(probeResultString.getBytes());
						probeResultFos.flush();
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
						uiHandler.appendToTextView(
								instructionView,
								"Fail to write to probeResult."
										+ e.getMessage());
					}
					// reset probe result
					probeResult.result = 0;
				}

			}, 11 * 1000);

		}

	}

}