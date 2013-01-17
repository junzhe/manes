package org.whispercomm.manes.client.macentity.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;

import android.content.Context;
import android.util.Log;

/**
 * Receives packets via UDP from the server. Also responsible for period-port
 * punching to keep the UDP socket open through firewalls and NAT.
 * 
 * @author David R. Bild
 * @author David Adrian
 */
public class UdpPacketListener extends UdpServer {
	private static final String TAG = UdpPacketListener.class.getName();

	private static final int REMOTE_PORT = 7889;
	public static final int PERIOD_MS = 60 * 1000; // 60 seconds

	private final PacketManager packetManager;
	private final IdManager idManager;

	public UdpPacketListener(Context context, PacketManager packetManager,
			IdManager idManager) {
		this.packetManager = packetManager;
		this.idManager = idManager;
		this.registerListener();
	}

	@Override
	public void start() {
		Log.v(TAG, "Started UDP Packet Listener");
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
	}

	public void sendKeepalive() throws KeepaliveFailureException,
			NotRegisteredException {
		if (getState() == State.STARTED) {
			DatagramPacket packet;
			try {
				packet = buildPacket();
			} catch (UnknownHostException e) {
				throw new KeepaliveFailureException(e);
			}
			// Send if created
			try {
				UdpPacketListener.this.send(packet);
			} catch (IOException e) {
				throw new KeepaliveFailureException(e);
			}
		}
	}

	private DatagramPacket buildPacket() throws NotRegisteredException,
			UnknownHostException {
		byte[] data = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(data);
		buf.putLong(idManager.getUserId());
		return new DatagramPacket(data, data.length,
				InetAddress.getByName(ManesService.SERVER_ADDRESS), REMOTE_PORT);
	}

	private void registerListener() {
		this.addUdpServerListener(new Listener() {
			@Override
			public void packetReceived(Event evt) {
				handleUdpPacket(evt.getPacketAsBytes());
			}
		});
	}

	protected void handleUdpPacket(byte[] packet) {
		Log.v(TAG, "Handling UDP packet");
		ByteBuffer buf = ByteBuffer.wrap(packet);
		@SuppressWarnings("unused")
		long senderId = buf.getLong();
		long appId = buf.getLong();
		@SuppressWarnings("unused")
		Date timestamp = new Date(buf.getLong());
		int size = buf.getInt();
		if (size > buf.remaining()) // Drop packet on invalid size
			return;
		byte[] contents = new byte[size];
		buf.get(contents);

		// TODO: standardize on AppId type: int or long
		packetManager.handleSinglePacket((int) appId, contents);
	}

}
