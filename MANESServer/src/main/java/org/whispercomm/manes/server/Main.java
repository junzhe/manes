package org.whispercomm.manes.server;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.event.DomainEvents;
import org.whispercomm.manes.server.event.NewPacketCreated;
import org.whispercomm.manes.server.module.LoggingConfigurationModule;
import org.whispercomm.manes.server.module.OAuthModule;
import org.whispercomm.manes.server.module.PortPunchModule;
import org.whispercomm.manes.server.module.SerializationModule;
import org.whispercomm.manes.server.module.ServerModule;
import org.whispercomm.manes.server.module.StoreModule;
import org.whispercomm.manes.server.module.TopologyTableModule;
import org.whispercomm.manes.server.module.VoldemortModule;
import org.whispercomm.manes.server.udp.PacketPusher;
import org.whispercomm.manes.server.udp.PortPunchServer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Main class for MANES server in staging or production.
 * 
 * @author David R. Bild
 * 
 */
public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		Logger logger = LoggerFactory
				.getLogger("org.whispercomm.manes.server.Main");
		try {
			Injector injector = Guice.createInjector(Stage.PRODUCTION,
					new LoggingConfigurationModule(), new StoreModule(),
					new OAuthModule(), new VoldemortModule(),
					new ServerModule(), new PortPunchModule(), new SerializationModule(),
					new TopologyTableModule());

			DomainEvents.clearHandlers();
			DomainEvents.register(NewPacketCreated.class,
					injector.getInstance(PacketPusher.class));
			ExecutorService executor = injector
					.getInstance(ExecutorService.class);
			ExternalServer external = injector
					.getInstance(ExternalServer.class);
			InternalServer internal = injector
					.getInstance(InternalServer.class);
			PortPunchServer punch = injector.getInstance(PortPunchServer.class);

			punch.start();
			internal.start();
			external.start();

			internal.join();
			external.join();
			executor.shutdown();
			while (!executor.isTerminated()) {
				// Wait to finish
			}
			punch.stop();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
