package org.whispercomm.manes.exp.locationsensor.server.modules;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.inject.AbstractModule;

/**
 * Guice module to configure logging.
 * production.
 * 
 * @author David R. Bild
 * 
 */
public class LoggingConfigurationModule extends AbstractModule {

	@Override
	protected void configure() {
		bridgeJulOverSlf4j();
	}
	
	/**
	 * 
	 */
	private void bridgeJulOverSlf4j() {
		// Disable the the default console handler for java.util.logging
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		rootLogger.removeHandler(handlers[0]);
		
		// Enable the SL4FJ bridge to capture java.util.logging calls.
		SLF4JBridgeHandler.install();
	}

}
