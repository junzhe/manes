package org.whispercomm.manes.exp.locationsensor.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.exp.locationsensor.server.modules.LoggingConfigurationModule;
import org.whispercomm.manes.exp.locationsensor.server.modules.ServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Main class for LocationSensor server in staging or production.
 * 
 * @author Yue Liu
 * 
 */
public class Main {

	public static void main(String[] args) {
		Logger logger = LoggerFactory
				.getLogger("org.whispercomm.manes.exp.locationsensor.server.Main");
		logger.info("Start running the server...");
		try {
			Injector injector = Guice.createInjector(Stage.PRODUCTION,
					new ServerModule(),
					new LoggingConfigurationModule());
			LocationSensorServer topo = injector.getInstance(LocationSensorServer.class);
			topo.start();
			topo.join();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
