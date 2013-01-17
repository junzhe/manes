package org.whispercomm.manes.topology.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.NonStaticGuiceFilter;

public class EmbeddedServer extends Server{

	/**
	 * Milliseconds allowed to running threads for graceful shutdown.
	 */
	private static final int SHUTDOWN_TIME = 500;

	private final ServletContextHandler handler;

	/**
	 * 
	 * @param port
	 */
	public EmbeddedServer(int port, String contextPath) {
		super(port);

		// Configure graceful shutdown
		this.setGracefulShutdown(SHUTDOWN_TIME);

		// Create the context handler
		handler = new ServletContextHandler(this, contextPath);

		// Required for embedded Jetty.
		handler.addServlet(DefaultServlet.class, "/");
	}

	/**
	 * Should be called by a subclass to register the injector and install Guice
	 * ServletModules.
	 * 
	 * @param injector
	 *            the parent injector from which the injector associated with
	 *            this server will be created.
	 * @param modules
	 *            any modules (e.g., ServerModule) that should be installed to
	 *            the child injector associated with this server.
	 */
	public void configureGuice(final Injector injector, final Module... modules) {
		final Injector childInjector = injector.createChildInjector(modules);

		// Add the Guice listener
		handler.addEventListener(new GuiceServletContextListener() {
			@Override
			protected Injector getInjector() {
				return childInjector;
			}
		});

		// Filter all requests through Guice
		// handler.addFilter(GuiceFilter.class, "*", null);
		handler.addFilter(
				new FilterHolder(childInjector
						.getInstance(NonStaticGuiceFilter.class)), "/*", null);
	}
}
