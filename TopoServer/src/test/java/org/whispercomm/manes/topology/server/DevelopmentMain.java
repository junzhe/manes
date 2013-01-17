package org.whispercomm.manes.topology.server;

import org.whispercomm.manes.topology.server.modules.LoggingConfigurationModule;
import org.whispercomm.manes.topology.server.modules.ServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Main class for MANES server running on local developer machine.
 * <p>
 * Uses an in-memory database.
 * 
 * @author David R. Bild
 * @author Junzhe Zhang
 * 
 */
public class DevelopmentMain {

	TopoServer topo;

	public DevelopmentMain() throws Exception {
		Injector injector = Guice.createInjector(Stage.PRODUCTION,new ServerModule(), new LoggingConfigurationModule());
		topo = injector.getInstance(TopoServer.class);
	}

	public void start() throws Exception {
		topo.start();
	}

	public boolean await() throws InterruptedException {
		while (topo.isStarting()) {
			Thread.sleep(100);
		}

		if (topo.isStarted()) {
			return true;
		} else {
			return false;
		}
	}

	public void stop() throws Exception {
		topo.stop();
		this.join();
	}

	public void join() throws InterruptedException {
		topo.join();
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
