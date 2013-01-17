package org.whispercomm.manes.topology.server;

import javax.inject.Inject;

import org.whispercomm.manes.topology.server.modules.*;

import com.google.inject.Injector;

/**
 * Embedded jetty server configured to serve the externally-visible endpoints of
 * MANES.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 * 
 */
public class TopoServer extends EmbeddedServer {
	private static final int PORT_NUMBER = 7890;
	private static final String ROOT_PATH = "/";

	@Inject
	public TopoServer(Injector injector) {
		super(PORT_NUMBER, ROOT_PATH);
		this.configureGuice(injector, new TopoServletModule());
	}

}
