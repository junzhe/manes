package org.whispercomm.manes.exp.testconnectivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.JSONException;
import org.whispercomm.manes.exp.testconnectivity.MainActivity.ProbResult;

import android.util.Log;

public class Receiver implements Runnable {

	static final String TAG = "testactivity.receiver";
	static final int CHECK_PERIOD = 60 * 1000;// check whether the application
												// is stopped every 1 minute

	SyncHelper syncHelper;
	ProbResult probeResult;

	public Receiver(SyncHelper syncHelper, ProbResult probeResult) {
		this.syncHelper = syncHelper;
		this.probeResult = probeResult;
	}

	@Override
	public void run() {
		byte[] recvBuf = new byte[512];
		DatagramPacket recvPck = new DatagramPacket(recvBuf, recvBuf.length);
		try {
			syncHelper.ms.setSoTimeout(CHECK_PERIOD);
		} catch (SocketException e1) {
			Log.e(TAG, e1.getMessage());
			syncHelper.uiHandler.appendToTextView(syncHelper.instructionView,
					e1.getMessage());
			return;
		}

		while (syncHelper.isAppRunning) {
			try {
				syncHelper.ms.receive(recvPck);
			} catch (SocketTimeoutException e) {
				// No message received
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Socket timed out with no incoming message.\n");
				continue;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Receive fails due to ioexception.\n");
				continue;
			}
			syncHelper.uiHandler.appendToTextView(
					syncHelper.instructionView,
					"Received a new message.\n");
			// check the recv pck
			Message recvMsg;
			int msgType;
			try {
				recvMsg = new Message(recvBuf, recvPck.getLength());
				msgType = recvMsg.getInt("type");
			} catch (JSONException e) {
				Log.i(TAG, e.getMessage());
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Cannot decode the received message.\n"
								+ e.getMessage());
				continue;
			}
			switch (msgType) {
			case Message.SYNC:
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Received a new SYNC message.\n");
				// record the startTime
				syncHelper.startTime = System.currentTimeMillis();
				// send back a sync_ack message
				byte[] syncAckBytes;
				try {
					syncAckBytes = new Message(Message.SYNC_ACK).toBytes();
					DatagramPacket syncAck = new DatagramPacket(syncAckBytes,
							syncAckBytes.length, syncHelper.group,
							MainActivity.PORT);
					syncHelper.ms.send(syncAck);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					syncHelper.uiHandler.appendToTextView(
							syncHelper.instructionView,
							"Cannot encode the SYNC_ACK message.\n"
									+ e.getMessage());
					continue;
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					syncHelper.uiHandler.appendToTextView(
							syncHelper.instructionView,
							"Cannot send the SYNC_ACK message.\n"
									+ e.getMessage());
					continue;
				}
				// bring alive the start button
				syncHelper.uiHandler.enableButton(syncHelper.start, true);
				break;
			case Message.PROBE:
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Received a new PROBE message.\n");
				// update the probe result
				probeResult.result = 1;
				break;
			}
		}
	}

}
