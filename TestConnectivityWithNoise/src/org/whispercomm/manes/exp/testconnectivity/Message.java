package org.whispercomm.manes.exp.testconnectivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * Various messages for synchronization and measurement purposes.
 * 
 * @author Yue Liu
 * 
 */
public class Message extends JSONObject {

	public static final int SYNC = 0;
	public static final int SYNC_ACK = 1;
	public static final int PROBE = 2;

	public Message(int type) throws JSONException {
		super();
		this.put("type", type);
	}

	public Message(byte[] msgBytes, int len) throws JSONException {
		super(new String(msgBytes, 0, len));
	}

	public byte[] toBytes() {
		return this.toString().getBytes();
	}
}
