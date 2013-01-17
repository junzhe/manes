package org.whispercomm.manes.exp.locationsensor.server;

import javax.inject.Inject;

import org.whispercomm.manes.exp.locationsensor.server.modules.*;

import com.google.inject.Injector;

/**
 * Embedded jetty server configured to serve the externally-visible endpoints of
 * location sensor server.
 * 
 * @author Yue Liu
 * 
 */
public class LocationSensorServer extends EmbeddedServer {
	private static final int PORT_NUMBER = 8890;
	private static final String ROOT_PATH = "/";

	@Inject
	public LocationSensorServer(Injector injector) {
		super(PORT_NUMBER, ROOT_PATH);
		this.configureGuice(injector, new LocSensorServletModule());
	}

}
