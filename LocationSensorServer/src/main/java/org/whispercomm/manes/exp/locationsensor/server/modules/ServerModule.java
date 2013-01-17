package org.whispercomm.manes.exp.locationsensor.server.modules;

import org.whispercomm.manes.exp.locationsensor.server.LocationSensorServer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Registers the {@link LocationSensorServer} with Guice as application singletons.
 * 
 * @author Yue Liu
 * 
 */
public class ServerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(LocationSensorServer.class).in(Singleton.class);
		}

}
