package org.whispercomm.manes.server;

import org.whispercomm.manes.server.event.DomainEvents;
import org.whispercomm.manes.server.module.InMemoryDatabaseModule;
import org.whispercomm.manes.server.module.LoggingConfigurationModule;
import org.whispercomm.manes.server.module.PortPunchModule;
import org.whispercomm.manes.server.module.ServerModule;
import org.whispercomm.manes.server.udp.PortPunchServer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Main class for MANES server running on local developer machine.
 * <p>
 * Uses an in-memory database.
 * 
 * @author David R. Bild
 * 
 */
public class DevelopmentMain {

	ExternalServer external;
	InternalServer internal;
	PortPunchServer punch;

	public DevelopmentMain() {
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT,
				new LoggingConfigurationModule(), new InMemoryDatabaseModule(),
				new ServerModule(), new PortPunchModule());

		DomainEvents.clearHandlers();
		// TODO Register Voldemort event handlers

		external = injector.getInstance(ExternalServer.class);
		internal = injector.getInstance(InternalServer.class);
		punch = injector.getInstance(PortPunchServer.class);
	}

	public void start() throws Exception {
		internal.start();
		external.start();
		punch.start();
	}

	public boolean await() throws InterruptedException {
		while (internal.isStarting() || external.isStarting()) {
			Thread.sleep(100);
		}

		if (internal.isStarted() && external.isStarted()) {
			return true;
		} else {
			return false;
		}
	}

	public void stop() throws Exception {
		punch.stop();
		internal.stop();
		external.stop();
		this.join();

	}

	public void join() throws InterruptedException {
		internal.join();
		external.join();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		DevelopmentMain main = new DevelopmentMain();
		main.start();
		main.join();
	}
}
