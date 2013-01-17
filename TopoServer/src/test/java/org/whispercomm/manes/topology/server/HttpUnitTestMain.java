package org.whispercomm.manes.topology.server;

import org.whispercomm.manes.topology.server.modules.HttpUnitTestModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class HttpUnitTestMain {
	TopoServer topo;

	public HttpUnitTestMain() throws Exception {
		Injector injector = Guice.createInjector(Stage.PRODUCTION,new HttpUnitTestModule());
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
		HttpUnitTestMain main = new HttpUnitTestMain();
		main.start();
		main.join();
	}
}
