package org.whispercomm.manes.exp.testconnectivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.JSONException;

import android.util.Log;

/**
 * Runnable in charge of synchronization.
 * 
 * @author Yue Liu
 * 
 */
public class Synchronizer implements Runnable {

	static final String TAG = "testconnectivity.synchronizer";
	static final int RECV_TIMEOUT = 1000; // 1s receive timeout

	SyncHelper syncHelper;

	public Synchronizer(SyncHelper syncHelper) {
		this.syncHelper = syncHelper;
	}

	/**
	 * Synchronization fails for various reasons. Notify the user to retry.
	 * 
	 * @param msg
	 *            error message
	 */
	void notifyRetry(String msg) {
		syncHelper.uiHandler.appendToTextView(syncHelper.instructionView, msg
				+ "\nSynchronization fails. Try again.\n");

		syncHelper.startTime = -1;
		syncHelper.uiHandler.enableButton(syncHelper.sync, true);
	}

	@Override
	public void run() {

		// send out sync pck
		byte[] syncPckBytes;
		try {
			syncPckBytes = new Message(Message.SYNC).toBytes();
			DatagramPacket syncPck = new DatagramPacket(syncPckBytes,
					syncPckBytes.length, syncHelper.group, MainActivity.PORT);
			syncHelper.ms.send(syncPck);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			notifyRetry(e.getMessage());
			return;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			notifyRetry(e.getMessage());
			return;
		}
		// record start_time
		syncHelper.startTime = System.currentTimeMillis();
		// wait for ACK
		byte[] recvBuf = new byte[512];
		DatagramPacket recvPck = new DatagramPacket(recvBuf, recvBuf.length);
		try {
			syncHelper.ms.setSoTimeout(RECV_TIMEOUT);
		} catch (SocketException e1) {
			Log.e(TAG, e1.getMessage());
			notifyRetry(e1.getMessage());
			return;
		}
		while (true) {
			try {
				syncHelper.ms.receive(recvPck);
			} catch (SocketTimeoutException e) {
				// No sync_ack received
				notifyRetry("No SYNC_ACK received.");
				break;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				notifyRetry(e.getMessage());
				break;
			}
			// check the recv pck
			Message recvMsg;
			try {
				recvMsg = new Message(recvBuf, recvPck.getLength());
				if (recvMsg.getInt("type") != Message.SYNC_ACK) {
					syncHelper.uiHandler.appendToTextView(
							syncHelper.instructionView,
							"Received non-ACK message.");
					continue;
				}
				// sync_ack successfully received
				syncHelper.uiHandler.appendToTextView(
						syncHelper.instructionView,
						"Synchronization succeeds. "
								+ "Press start on both sender and receiver"
								+ " to schedule one round's measurement.");
				syncHelper.uiHandler.enableButton(syncHelper.start, true);
				break;
			} catch (JSONException e) {
				Log.i(TAG, e.getMessage());
				continue;
			}
		}

	}

}
