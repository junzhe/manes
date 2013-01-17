/**
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.servlet;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Updated version (commit fbbb52dcc92e) of {@link GuiceFilter} that allows
 * injection of an {@link FilterPipeline} object via a package-visible
 * constructor. This class when the latest release of Guice includes these
 * features.
 * <p>
 * Apply this filter in web.xml above all other filters (typically), to all
 * requests where you plan to use servlet scopes. This is also needed in order
 * to dispatch requests to injectable filters and servlets:
 * 
 * <pre>
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;<b>com.google.inject.servlet.UpdatedGuiceFilter</b>&lt;/filter-class&gt;
 *  &lt;/filter&gt;
 * 
 *  &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 * </pre>
 * 
 * This filter must appear before every filter that makes use of Guice injection
 * or servlet scopes functionality. Typically, you will only register this
 * filter in web.xml and register any other filters (and servlets) using a
 * {@link ServletModule}.
 * 
 * @author crazybob@google.com (Bob Lee)
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class UpdatedGuiceFilter implements Filter {
	static final ThreadLocal<Context> localContext = new ThreadLocal<Context>();
	static volatile FilterPipeline pipeline = new DefaultFilterPipeline();

	/**
	 * We allow both the static and dynamic versions of the pipeline to exist.
	 */
	private final FilterPipeline injectedPipeline;

	/** Used to inject the servlets configured via {@link ServletModule} */
	static volatile WeakReference<ServletContext> servletContext = new WeakReference<ServletContext>(
			null);

	private static final String MULTIPLE_INJECTORS_WARNING = "Multiple Servlet injectors detected. This is a warning "
			+ "indicating that you have more than one "
			+ UpdatedGuiceFilter.class.getSimpleName()
			+ " running "
			+ "in your web application. If this is deliberate, you may safely "
			+ "ignore this message. If this is NOT deliberate however, "
			+ "your application may not work as expected.";

	private static final Logger LOGGER = Logger
			.getLogger(UpdatedGuiceFilter.class.getName());

	public UpdatedGuiceFilter() {
		// Use the static FilterPipeline
		this(null);
	}

	@Inject
	UpdatedGuiceFilter(FilterPipeline filterPipeline) {
		injectedPipeline = filterPipeline;
	}

	// VisibleForTesting
	@Inject
	static void setPipeline(FilterPipeline pipeline) {

		// This can happen if you create many injectors and they all have their
		// own
		// servlet module. This is legal, caveat a small warning.
		if (UpdatedGuiceFilter.pipeline instanceof ManagedFilterPipeline) {
			LOGGER.warning(MULTIPLE_INJECTORS_WARNING);
		}

		// We overwrite the default pipeline
		UpdatedGuiceFilter.pipeline = pipeline;
	}

	// VisibleForTesting
	static void reset() {
		pipeline = new DefaultFilterPipeline();
		localContext.remove();
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		FilterPipeline filterPipeline = getFilterPipeline();

		Context previous = UpdatedGuiceFilter.localContext.get();
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpServletRequest originalRequest = (previous != null) ? previous
				.getOriginalRequest() : request;
		localContext.set(new Context(originalRequest, request, response));
		try {
			// dispatch across the servlet pipeline, ensuring web.xml's
			// filterchain is honored
			filterPipeline.dispatch(servletRequest, servletResponse,
					filterChain);
		} finally {
			localContext.set(previous);
		}
	}

	static HttpServletRequest getOriginalRequest() {
		return getContext().getOriginalRequest();
	}

	static HttpServletRequest getRequest() {
		return getContext().getRequest();
	}

	static HttpServletResponse getResponse() {
		return getContext().getResponse();
	}

	static ServletContext getServletContext() {
		return servletContext.get();
	}

	private static Context getContext() {
		Context context = localContext.get();
		if (context == null) {
			throw new OutOfScopeException(
					"Cannot access scoped object. Either we"
							+ " are not currently inside an HTTP Servlet request, or you may"
							+ " have forgotten to apply "
							+ UpdatedGuiceFilter.class.getName()
							+ " as a servlet filter for this request.");
		}
		return context;
	}

	static class Context {
		final HttpServletRequest originalRequest;
		final HttpServletRequest request;
		final HttpServletResponse response;

		Context(HttpServletRequest originalRequest, HttpServletRequest request,
				HttpServletResponse response) {
			this.originalRequest = originalRequest;
			this.request = request;
			this.response = response;
		}

		HttpServletRequest getOriginalRequest() {
			return originalRequest;
		}

		HttpServletRequest getRequest() {
			return request;
		}

		HttpServletResponse getResponse() {
			return response;
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		final ServletContext servletContext = filterConfig.getServletContext();

		// Store servlet context in a weakreference, for injection
		UpdatedGuiceFilter.servletContext = new WeakReference<ServletContext>(
				servletContext);

		// In the default pipeline, this is a noop. However, if replaced
		// by a managed pipeline, a lazy init will be triggered the first time
		// dispatch occurs.
		FilterPipeline filterPipeline = getFilterPipeline();
		filterPipeline.initPipeline(servletContext);
	}

	public void destroy() {

		try {
			// Destroy all registered filters & servlets in that order
			FilterPipeline filterPipeline = getFilterPipeline();
			filterPipeline.destroyPipeline();

		} finally {
			reset();
			servletContext.clear();
		}
	}

	private FilterPipeline getFilterPipeline() {
		// Prefer the injected pipeline, but fall back on the static one for
		// web.xml users.
		return (null != injectedPipeline) ? injectedPipeline : pipeline;
	}
}