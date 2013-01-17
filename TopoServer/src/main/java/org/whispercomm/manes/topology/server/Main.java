package org.whispercomm.manes.topology.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.module.APClientTableModule;
import org.whispercomm.manes.server.module.ClientLocationTableModule;
import org.whispercomm.manes.server.module.GPSGridClientTableModule;
import org.whispercomm.manes.server.module.OAuthModule;
import org.whispercomm.manes.server.module.SerializationModule;
import org.whispercomm.manes.server.module.TopologyTableModule;
import org.whispercomm.manes.server.module.VoldemortModule;
import org.whispercomm.manes.topology.server.modules.DataInterfaceModule;
import org.whispercomm.manes.topology.server.modules.LoggingConfigurationModule;
import org.whispercomm.manes.topology.server.modules.ServerModule;
import org.whispercomm.manes.topology.server.modules.TopologyManagerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Main class for MANES server in staging or production.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 * 
 */
public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		Logger logger = LoggerFactory
				.getLogger("org.whispercomm.manes.topology.server.Main");
		try {
			Injector injector = Guice.createInjector(Stage.PRODUCTION,
					new ServerModule(), new TopologyManagerModule(),
					new DataInterfaceModule(), new SerializationModule(),
					new LoggingConfigurationModule(), new VoldemortModule(),
					new OAuthModule(), new APClientTableModule(),
					new ClientLocationTableModule(),
					new GPSGridClientTableModule(), new TopologyTableModule());
			TopoServer topo = injector.getInstance(TopoServer.class);
			topo.start();
			topo.join();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
