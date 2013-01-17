package org.whispercomm.manes.server.udp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.whispercomm.manes.server.domain.LinkTuple;
import org.whispercomm.manes.server.domain.Packet;
import org.whispercomm.manes.server.domain.User;

public class PacketRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PacketRunnable.class);

	private static final Logger PACKET_LOGGER = LoggerFactory
			.getLogger("log.packet");

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final int MAX_SIZE = 3000;
	private Packet packet;
	private Iterable<LinkTuple> links;
	private PortPunchServer server;
	private User sender;

	private JSONObject record;

	public PacketRunnable(User sender, Packet packet,
			Iterable<LinkTuple> links, PortPunchServer server) {
		this.packet = packet;
		this.links = links;
		this.server = server;
		this.sender = sender;
		this.record = new JSONObject();
	}

	@Override
	public void run() {
		try {
			byte[] contents = serializePacket(packet);
			JSONArray receiverRecords = new JSONArray();
			processReceivers(contents, receiverRecords);

			prepareJsonRecord(contents, receiverRecords);

			MDC.put("userid", String.valueOf(sender.getIdentifier()));
			PACKET_LOGGER.info(record.toString());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Unable to send invalid message", e);
		}
	}

	private byte[] serializePacket(Packet p) throws IllegalArgumentException {
		byte[] contents = p.getContents();
		int len = 8 + 8 + 8 + 4 + contents.length;

		if (len > MAX_SIZE)
			throw new IllegalArgumentException(
					String.format(
							"Maximum packet size exceeded. Dropping packet.  Got %d. Max is %d.",
							len, MAX_SIZE));

		ByteBuffer buf = ByteBuffer.allocate(len);
		buf.putLong(p.getSender().getIdentifier());
		buf.putLong(p.getApplicationId());
		buf.putLong(p.getTimeSent().getMillis());
		buf.putInt(contents.length);
		buf.put(contents);
		return buf.array();
	}

	private void processReceivers(byte[] contents, JSONArray receivers) {
		for (LinkTuple link : links) {
			boolean heard = link.getStrength() > RANDOM.nextDouble();
			receivers.add(buildReceiverRecord(link, heard));
			if (heard)
				sendPacket(link.getUser(), contents);
		}
	}

	private void sendPacket(User receiver, byte[] data) {
		InetAddress addr = receiver.getPortPunchAddress();
		int port = receiver.getPortPunchPort();
		if (addr != null) {
			try {
				server.send(new DatagramPacket(data, data.length, addr, port));
			} catch (IOException e) {
				LOGGER.warn("Unable to send pacekt to user {}",
						receiver.getIdentifier(), e);
			}
		}
	}

	private void prepareJsonRecord(byte[] contents, JSONArray receivers) {
		record.put("timestamp", System.currentTimeMillis());
		record.put("sender_id", sender.getIdentifier());
		record.put("contents", encodeContents(contents));
		record.put("receivers", receivers);
	}

	private JSONObject buildReceiverRecord(LinkTuple link, boolean heard) {
		JSONObject obj = new JSONObject();
		obj.put("id", link.getUser().getIdentifier());
		obj.put("strength", link.getStrength());
		obj.put("heard", heard);
		return obj;
	}

	private String encodeContents(byte[] contents) {
		try {
			return new String(Base64.encodeBase64(contents), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 better be supported
			throw new RuntimeException(e);
		}
	}

}
