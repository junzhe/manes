package org.whispercomm.manes.server;

import javax.inject.Inject;

import org.whispercomm.manes.server.module.ExternalManesServletModule;

import com.google.inject.Injector;

/**
 * Embedded jetty server configured to serve the externally-visible endpoints of
 * MANES.
 * 
 * @author David R. Bild
 * 
 */
public class ExternalServer extends EmbeddedServer {
	private static final int PORT_NUMBER = 7889;
	private static final String ROOT_PATH = "/";

	@Inject
	public ExternalServer(Injector injector) {
		super(PORT_NUMBER, ROOT_PATH);
		this.configureGuice(injector, new ExternalManesServletModule());
	}

}
