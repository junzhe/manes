package org.whispercomm.manes.topology.server.modules;

import org.whispercomm.manes.server.http.filter.AuthorizationResourceFilterFactory;
import org.whispercomm.manes.server.http.provider.OAuthProviderImpl;
import org.whispercomm.manes.server.http.provider.UserPathParam;
import org.whispercomm.manes.topology.server.http.TopologyResource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Configures the topology server.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 * 
 */
public class TopoServletModule extends JerseyServletModule {

	protected void configureInitParams(
			ImmutableMap.Builder<String, String> initParams) {
		// POJO mapping
		initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

		// Enable the OAuth filter. This will reject all requests with invalid
		// OAuth authentications. Requests will no OAuth authentication attempt
		// will still pass.
		initParams
				.put("com.sun.jersey.spi.container.ContainerRequestFilters",
						"org.whispercomm.manes.server.http.filter.LoggingOAuthServerFilter");
		initParams
				.put("com.sun.jersey.spi.container.ResourceFilters",
						"org.whispercomm.manes.server.http.filter.AuthorizationResourceFilterFactory");
	}

	@Override
	protected void configureServlets() {
		bind(GuiceContainer.class);
		bind(TopologyResource.class).in(Scopes.SINGLETON);

		// Bind OAuth providers
		bind(OAuthProviderImpl.class).in(Scopes.SINGLETON);
		bind(AuthorizationResourceFilterFactory.class).in(Scopes.SINGLETON);
		// PathParam that is used to get User object
		bind(UserPathParam.class).in(Scopes.SINGLETON);

		ImmutableMap.Builder<String, String> initParams = ImmutableMap
				.<String, String> builder();
		configureInitParams(initParams);
		serve("*").with(GuiceContainer.class, initParams.build());
	}

}
