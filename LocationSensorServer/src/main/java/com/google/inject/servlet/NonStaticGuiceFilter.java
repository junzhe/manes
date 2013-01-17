package com.google.inject.servlet;

import com.google.inject.Inject;

/**
 * Wrapper around {@link UpdatedGuiceFilter} to make publicly-visible the
 * constructor accepting an instance of {@link FilterPipeline}.
 * <p>
 * By default, {@link GuiceFilter} uses a static reference to a
 * {@code FilterPipeline} object. To support multiple applications with
 * different pipelines running under the same classloader, {@code GuiceFilter}
 * needs to use a non-static reference.
 * <p>
 * To use this class, instead of {@code GuiceFilter}, simple construct an
 * instance of it with the injector returned by
 * {@link com.google.inject.servlet.GuiceServletContextListener#getInjector()
 * getInjector} of the
 * {@link com.google.inject.servlet.GuiceServletContextListener
 * GuiceServletContextListener} object for the app and register it with
 * the context handler:
 * <p>
 * <pre>
 * // Create the context handler
 * ServletContextHandler handler = new ServletContextHandler();
 *
 * // Create the injector
 * final Injector injector = Guice.createInjector(new MyServletModule());
 *
 * // Add the Guice listener
 * handler.addEventListener(new GuiceServletContextListener() {
 *     &#64;Override
 *     protected Injector getInjector() {
 *				return injector;
 *			}
 *		});
 *
 * // Filter all requests through Guice
 * handler.addFilter(
 *         new FilterHolder(injector
 *                 .getInstance(NonStaticGuiceFilter.class)), "/*", null);
 * </pre>
 * 
 * @author David R. Bild
 * 
 */
public class NonStaticGuiceFilter extends UpdatedGuiceFilter {

	/**
	 * Do not use. Must inject a {@link FilterPipeline} via the constructor.
	 */
	@SuppressWarnings("unused")
	private NonStaticGuiceFilter() {
		throw new IllegalStateException();
	}

	@Inject
	public NonStaticGuiceFilter(FilterPipeline filterPipeline) {
		super(filterPipeline);
	}

}
