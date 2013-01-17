package org.whispercomm.manes.server;

import javax.inject.Inject;

import org.whispercomm.manes.server.module.InternalManesServletModule;

import com.google.inject.Injector;

public class InternalServer extends EmbeddedServer {
	private static final int PORT_NUMBER = 6889;
	private static final String ROOT_PATH = "/internal";

	@Inject
	public InternalServer(Injector injector) {
		super(PORT_NUMBER, ROOT_PATH);
		this.configureGuice(injector, new InternalManesServletModule());
	}

}
