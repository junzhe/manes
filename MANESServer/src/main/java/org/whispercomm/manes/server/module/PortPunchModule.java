package org.whispercomm.manes.server.module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.whispercomm.manes.server.udp.PacketPusher;
import org.whispercomm.manes.server.udp.PortPunchServer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Provides a configured {@link PortPuncherServer}.
 * 
 * @author David R. Bild
 * 
 */
public class PortPunchModule extends AbstractModule {

	private static final int SEND_THREADS = 128;

	@Override
	protected void configure() {
		bind(PortPunchServer.class).in(Singleton.class);
		bind(PacketPusher.class);
	}
	
	@Provides
	@Singleton
	ExecutorService provideExecutorService() {
		ExecutorService executor = Executors.newFixedThreadPool(SEND_THREADS);
		return executor;
	}

}
