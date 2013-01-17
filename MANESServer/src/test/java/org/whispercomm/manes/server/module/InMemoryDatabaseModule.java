package org.whispercomm.manes.server.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

/**
 * Guice module to configure an in-memory database useful for local developer
 * testing.
 * 
 * @author David R. Bild
 * 
 */
public class InMemoryDatabaseModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(InMemoryDatabaseModule.class);

	@Override
	protected void configure() {
		LOGGER.error("This module is not configured. Implement InMemoryDatabaseModule");
		// TODO Configure for Voldemort
	}

}
