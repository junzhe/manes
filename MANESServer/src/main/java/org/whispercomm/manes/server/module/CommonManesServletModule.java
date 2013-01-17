package org.whispercomm.manes.server.module;

import org.whispercomm.manes.server.http.provider.InvalidJsonBeanExceptionMapper;
import org.whispercomm.manes.server.http.provider.JsonMappingExceptionMapper;
import org.whispercomm.manes.server.http.provider.JsonParseExceptionMapper;
import org.whispercomm.manes.server.http.provider.UnrecognizedPropertyExceptionMapper;
import org.whispercomm.manes.server.http.provider.UserPathParam;
import org.whispercomm.manes.server.http.provider.ValidatingJsonProvider;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Abstract class that configures the superclass {@link JerseyServletModule}
 * with the common configuration for MANES. Child classes can extend the
 * configuration by implementing the {@link #bind} and {@link #init} methods.
 * 
 * @author David R. Bild
 * 
 */
public abstract class CommonManesServletModule extends JerseyServletModule {

	/**
	 * Implement this method to configure Guice bindings. This method is called
	 * by the {@link #configureServlets} method.
	 */
	protected abstract void configureBindings();

	/**
	 * Implement this method to set Jersey initialization parameters. This
	 * method is called by the {@link #configureServlets} method.
	 * 
	 * @param initParams
	 *            the map into which to put the initialization parameters.
	 */
	protected abstract void configureInitParams(
			ImmutableMap.Builder<String, String> initParams);

	@Override
	protected void configureServlets() {
		// Explicitly bind GuiceContainer so that the child injector to which
		// the following bindings are applied, and not the root injector,
		// is injected into its constructor, for it to search for resources and
		// providers.
		bind(GuiceContainer.class);

		// Bind Providers
		bind(ValidatingJsonProvider.class).in(Scopes.SINGLETON);
		bind(UserPathParam.class).in(Scopes.SINGLETON);

		// Bind Exception Mappers
		bind(InvalidJsonBeanExceptionMapper.class).in(Scopes.SINGLETON);
		bind(JsonMappingExceptionMapper.class).in(Scopes.SINGLETON);
		bind(JsonParseExceptionMapper.class).in(Scopes.SINGLETON);
		bind(UnrecognizedPropertyExceptionMapper.class).in(Scopes.SINGLETON);

		// Configure child-defined bindings
		configureBindings();

		// Set Jersey initialization parameters
		ImmutableMap.Builder<String, String> initParams = ImmutableMap
				.<String, String> builder();

		// Configure child-defined initialization parameters
		configureInitParams(initParams);

		// Serve all URLs with the Guice container.
		serve("*").with(GuiceContainer.class, initParams.build());
	}
}
