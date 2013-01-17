package org.whispercomm.manes.client.macentity.network;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.http.SendRequest;
import org.whispercomm.manes.client.macentity.http.SendResponseHandler;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.sun.jersey.oauth.signature.OAuthSignatureException;

public class PacketManager {

	private static final String TAG = PacketManager.class.getName();
	private static final String JSON_KEY_APP_ID = "app_id";
	private static final String JSON_KEY_CONTENTS = "contents";

	static final int PACKET_QUEUE_CAPACITY = 128;

	ConcurrentHashMap<Integer, PacketQueue<byte[]>> appQueues;
	IdManager idManager;
	HttpManager httpManager;
	Context context;

	public PacketManager(HttpManager httpManager, IdManager idManager,
			Context context) {
		this.appQueues = new ConcurrentHashMap<Integer, PacketQueue<byte[]>>();
		this.idManager = idManager;
		this.httpManager = httpManager;
		this.context = context;
	}

	public synchronized void addQueueUser(int appId) {
		PacketQueue<byte[]> queue = appQueues.get(appId);
		if (queue == null) {
			appQueues
					.put(appId, new PacketQueue<byte[]>(PACKET_QUEUE_CAPACITY));
			queue = appQueues.get(appId);
		}
		queue.addUser();
	}

	public synchronized void removeQueueUser(int appId) {
		PacketQueue<byte[]> queue = appQueues.get(appId);
		if (queue == null) {
			return;
		}
		queue.removeUser();
		if (queue.isUnused()) {
			appQueues.remove(appId);
		}
	}

	/**
	 * Return the oldest packet for the specified app id.
	 * 
	 * @param appId
	 * @param timeout
	 *            time in milliseconds to wait for a packet
	 * @return the packet, {@code null} on timeout.
	 * @throws InterruptedException
	 */
	public byte[] receive(int appId, long timeout) throws InterruptedException {
		PacketQueue<byte[]> queue = appQueues.get(appId);
		// Fetch one packet from the front of the queue
		// and wait if there is no packet available
		byte[] packet = null;
		if (queue != null) {
			packet = queue.poll(timeout, TimeUnit.MILLISECONDS);
		}
		return packet;
	}

	/**
	 * Sends a packet to the MANES server to be broadcast to all in-range
	 * clients.
	 * <p>
	 * The send is attempted once and failures are ignored, as they would be in
	 * a true wireless broadcast environment.
	 * 
	 * @param appId
	 *            the app identifier to attach to the outgoing packet.
	 * @param contents
	 *            the contents of the outgoing packet.
	 * @throws NotRegisteredException
	 *             if the client has not registered with the MANES server.
	 */
	public void send(int appId, byte[] contents) throws NotRegisteredException {
		SendRequest request = new SendRequest(idManager.getUserId(), appId,
				contents);
		try {
			HttpManager.signRequest(request, idManager.getUserId(),
					idManager.getSharedSecret());
		} catch (OAuthSignatureException e) {
			// This should only happen due to buggy code, so it should be caught
			// in testing.
			Log.e(TAG, "Unable to sign send request with OAuth", e);
		}
		SendResponseHandler handler = new SendResponseHandler();
		httpManager.submit(request, handler);
	}

	/**
	 * Helper function for handling an individual packet encoded as a JSON
	 * object from the REST API
	 * 
	 * @param jObject
	 * @throws JSONException
	 * 
	 * @deprecated Packets received as {@code byte[]} over UDP
	 */
	void handleSinglePacket(JSONObject jObject) throws JSONException {
		int appId = jObject.getInt(JSON_KEY_APP_ID);
		String contentString = jObject.getString(JSON_KEY_CONTENTS);
		byte[] contents = Base64.decode(contentString, Base64.DEFAULT);
		storePacket(appId, contents);
	}

	/**
	 * Adds the packet to the queue for the specified app id, creating the queue
	 * if necessary. The packet is dropped if no applications are registered to
	 * the app id.
	 * 
	 * @param appId
	 *            application id specified in the packet
	 * @param contents
	 *            contents of the packet
	 */
	protected void storePacket(int appId, byte[] contents) {
		handleSinglePacket(appId, contents);
	}

	/**
	 * Add a packet with the specific application ID to its corresponding queue.
	 * 
	 * @param appId
	 * @param contents
	 *            as a byte array
	 */
	void handleSinglePacket(int appId, byte[] contents) {
		PacketQueue<byte[]> queue = appQueues.get(appId);
		if (queue == null) // No client for this app id,
			return; // so drop the packet

		// Put the new packet in the tail of this queue
		// If the queue is full, remove the head of the queue and put again
		while (queue.offer(contents) == false) {
			queue.poll();
		}
	}

}
