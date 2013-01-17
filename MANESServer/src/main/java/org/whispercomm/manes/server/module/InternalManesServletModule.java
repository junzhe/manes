package org.whispercomm.manes.server.module;

import org.whispercomm.manes.server.http.TopologyResource;

import com.google.common.collect.ImmutableMap.Builder;

/**
 * Configures the servlets to serve the internally-visible private API
 * endpoints.
 * 
 * @author David R. Bild
 * 
 */
public class InternalManesServletModule extends CommonManesServletModule {

	@Override
	protected void configureBindings() {
		// Bind Resources
		bind(TopologyResource.class);
	}

	@Override
	protected void configureInitParams(Builder<String, String> initParams) {
	}

}
