package org.whispercomm.manes.server.udp;

import java.util.concurrent.ExecutorService;

import org.whispercomm.manes.server.domain.LinkTuple;
import org.whispercomm.manes.server.domain.Packet;
import org.whispercomm.manes.server.event.EventHandler;
import org.whispercomm.manes.server.event.NewPacketCreated;

import com.google.inject.Inject;

public class PacketPusher implements EventHandler<NewPacketCreated> {

	private ExecutorService executor;
	private PortPunchServer server;

	@Inject
	public PacketPusher(PortPunchServer server, ExecutorService executor) {
		this.executor = executor;
		this.server = server;
	}

	@Override
	public void handle(NewPacketCreated event) {
		Packet packet = event.getPacket();
		Iterable<LinkTuple> links = event.getTuple();
		PacketRunnable sendTask = new PacketRunnable(event.getSender(), packet,
				links, server);
		executor.execute(sendTask);
	}

}
