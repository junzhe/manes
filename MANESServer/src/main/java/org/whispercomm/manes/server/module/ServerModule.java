package org.whispercomm.manes.server.module;

import org.whispercomm.manes.server.ExternalServer;
import org.whispercomm.manes.server.InternalServer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Registers the {@link ExternalServer} and {@link InternalServer} classes with
 * Guice as application singletons.
 * 
 * @author David R. Bild
 * 
 */
public class ServerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ExternalServer.class).in(Singleton.class);
		bind(InternalServer.class).in(Singleton.class);
	}

}
