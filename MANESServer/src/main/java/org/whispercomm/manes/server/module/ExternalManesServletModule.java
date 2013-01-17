package org.whispercomm.manes.server.module;

import org.whispercomm.manes.server.http.PacketResource;
import org.whispercomm.manes.server.http.UserResource;
import org.whispercomm.manes.server.http.filter.AuthorizationResourceFilterFactory;
import org.whispercomm.manes.server.http.provider.OAuthProviderImpl;

import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Scopes;

/**
 * Configures the servlets to serve the externally-visible public API endpoints.
 * 
 * @author David R. Bild
 * 
 */
public class ExternalManesServletModule extends CommonManesServletModule {

	@Override
	protected void configureBindings() {
		// Bind Resources
		bind(UserResource.class).in(Scopes.SINGLETON);
		bind(PacketResource.class).in(Scopes.SINGLETON);

		// Bind OAuth providers
		bind(OAuthProviderImpl.class).in(Scopes.SINGLETON);
		bind(AuthorizationResourceFilterFactory.class).in(Scopes.SINGLETON);
	}

	@Override
	protected void configureInitParams(Builder<String, String> initParams) {
		// Enable the OAuth filter. This will reject all requests with invalid
		// OAuth authentications. Requests will no OAuth authentication attempt
		// will still pass.
		initParams
				.put("com.sun.jersey.spi.container.ContainerRequestFilters",
						"org.whispercomm.manes.server.http.filter.LoggingOAuthServerFilter");

		// Exclude the new user registration URL from the OAuth filter.
		initParams.put(
				"com.sun.jersey.config.property.oauth.ignorePathPattern",
				"user/");

		initParams
				.put("com.sun.jersey.spi.container.ResourceFilters",
						"org.whispercomm.manes.server.http.filter.AuthorizationResourceFilterFactory");
	}

}
