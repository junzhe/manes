package org.whispercomm.manes.server.udp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.DoesNotExistException;
import org.whispercomm.manes.server.domain.User;

/**
 * Listens to incoming UDP requests from clients, recording the IP address and
 * port number.
 * 
 * @author David R. Bild
 * 
 */
public class PortPunchServer extends UdpServer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PortPunchServer.class);

	private static final int PORT = 7889;

	private final DataService dataService;

	@Inject
	public PortPunchServer(DataService dataService) {
		this.dataService = dataService;
		this.setPort(PORT);
		registerListener();
	}

	private void registerListener() {
		this.addUdpServerListener(new PortPunchListener(dataService));
	}

	private static class PortPunchListener implements Listener {

		private final DataService dataService;

		public PortPunchListener(DataService dataService) {
			this.dataService = dataService;
		}

		@Override
		public void packetReceived(Event evt) {
			DatagramPacket pkt = evt.getPacket();
			ByteBuffer buf = ByteBuffer.wrap(pkt.getData());

			// TODO: validate packet contents and authenticate user.
			InetAddress ip = pkt.getAddress();
			int port = pkt.getPort();
			long userId = buf.getLong(); // TODO Switch to integer

			try {
				User user = dataService.getUser((int) userId);
				user.setPortPunch(ip, port);
				user.writeBack();
				LOGGER.debug("Updated port-punch for user id {}: {}:{}",
						new Object[] { userId, ip.toString(), port });
			} catch (DoesNotExistException e) {
				LOGGER.info(
						"Received port-punch packet from invalid user id: {}",
						userId);
			}

		}

	}

}
