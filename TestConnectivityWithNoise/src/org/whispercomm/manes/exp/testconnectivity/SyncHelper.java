package org.whispercomm.manes.exp.testconnectivity;

import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

/**
 * Synchronization related data
 * 
 * @author Yue Liu
 * 
 */
public class SyncHelper {

	UiHandler uiHandler;
	public long startTime;
	public Button start;
	public Button sync;
	TextView instructionView;
	MulticastSocket ms;
	InetAddress group;
	public boolean isAppRunning; // Piggbacked state variable indicating whether
									// the

	// application is still running

	public SyncHelper(UiHandler uiHandler, Button start, Button sync, 
			TextView instructionView, MulticastSocket ms, InetAddress group) {
		this.uiHandler = uiHandler;
		this.startTime = -1;
		this.start = start;
		this.sync = sync;
		this.instructionView = instructionView;
		this.ms = ms;
		this.group = group;
		this.isAppRunning = true;
	}

}
