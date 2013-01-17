package org.whispercomm.manes.client.macentity.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.sun.jersey.oauth.signature.HMAC_SHA1;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthRequest;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.sun.jersey.oauth.signature.OAuthSignatureMethod;
import com.sun.jersey.spi.service.ServiceFinder;

/**
 * 
 * @author David Adrian
 * @author David R. Bild
 * 
 */
public class HttpManager {
	private static final String TAG = HttpManager.class.getSimpleName();

	/**
	 * Time to wait for executor service tasks to finish in milliseconds.
	 */
	private static final int SHUTDOWN_WAIT_MS = 1;
	private static final int DEFAULT_THREAD_COUNT = 4;

	private static final int DEFAULT_HTTP_PORT = 80;
	private static final int DEFAULT_HTTPS_PORT = 443;

	/**
	 * @return a default instance of {@link ScheduledExecutorService}.
	 */
	private static ScheduledExecutorService createScheduledExecutorService() {
		return new ScheduledThreadPoolExecutor(DEFAULT_THREAD_COUNT);
	}

	/**
	 * @return a default instance of {@link HttpClient};
	 */
	private static HttpClient createHttpClient() {
		HttpParams connParams = new BasicHttpParams();
		ConnManagerParams.setMaxConnectionsPerRoute(connParams,
				new ConnPerRouteBean(DEFAULT_THREAD_COUNT));
		ConnManagerParams.setMaxTotalConnections(connParams,
				DEFAULT_THREAD_COUNT);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), DEFAULT_HTTP_PORT));
		schemeRegistry.register(new Scheme("https", PlainSocketFactory
				.getSocketFactory(), DEFAULT_HTTPS_PORT));

		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(
				connParams, schemeRegistry);

		HttpParams params = new BasicHttpParams();

		return new DefaultHttpClient(connManager, params);
	}

	private final HttpClient httpClient;
	private final ScheduledExecutorService executor;

	/**
	 * Construct a default HttpManager using a default
	 * {@link ThreadSafeClientConnManager} and a default
	 * {@link ScheduledThreadPoolExecutor}.
	 */
	public HttpManager() {
		this(createHttpClient(), createScheduledExecutorService());
	}

	private HttpManager(HttpClient httpClient, ScheduledExecutorService executor) {
		this.httpClient = httpClient;
		this.executor = executor;
	}

	/**
	 * Shutdown the resources managed by this instance.
	 */
	public void shutdown() {
		Log.i(TAG, "shutdown() called.");
		// Try to let executing tasks finish.
		this.executor.shutdown();
		boolean terminated = false;
		try {
			terminated = this.executor.awaitTermination(SHUTDOWN_WAIT_MS,
					TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// Ignore, since tasks are force killed next.
		}
		// Kill them if the didn't finish quickly.
		if (!terminated) {
			Log.i(TAG, "calling shutdownNow() on ScheduledExecutorService.");
			this.executor.shutdownNow();
		}

		this.httpClient.getConnectionManager().shutdown();
		Log.i(TAG, "Shutdown complete.");
	}

	/**
	 * Execute the given request. The HTTP response is handled by the supplied
	 * {@link ResponseHandler}. This call will block until a response is
	 * received.
	 * <p>
	 * This method delegates to
	 * {@link HttpClient#execute(HttpUriRequest, ResponseHandler)}.
	 * 
	 * @param request
	 *            the HTTP request
	 * @param handler
	 *            the response handler
	 * @return the return value from the response handler
	 * @throws IOException
	 * @throws ClientProtocolException
	 * 
	 */
	public <T> T execute(HttpUriRequest request, ResponseHandler<T> handler)
			throws ClientProtocolException, IOException {
		return httpClient.execute(request, handler);
	}

	/**
	 * Schedules the given request to executed after the specified delay. The
	 * HTTP response is handled by the supplied {@link ResponseHandler}. The
	 * result is available asynchronously via the returned {@link Future}.
	 * <p>
	 * Execution is delegated to
	 * {@link HttpClient#execute(HttpUrirequest, ResponseHandler)}.
	 * 
	 * @param request
	 *            the HTTP request
	 * @param handler
	 *            the response handler
	 * @param delay
	 *            the time from now to delay execution
	 * @param unit
	 *            the time unit of the delay parameter
	 * @return a {@code Future} wrapping the return value from the response
	 *         handler
	 */
	public <T> Future<T> schedule(final HttpUriRequest request,
			final ResponseHandler<T> handler, long delay, TimeUnit unit) {
		Callable<T> task = new Callable<T>() {
			@Override
			public T call() throws Exception {
				return HttpManager.this.execute(request, handler);
			}
		};
		return executor.schedule(task, delay, unit);
	}

	/**
	 * Schedules the given request to be executed as soon as possible. The HTTP
	 * response is handled by the supplied {@link ResponseHandler}. The result
	 * is available asynchronously via the returned {@link Future}.
	 * <p>
	 * Execution is delegated to
	 * {@link HttpClient#execute(HttpUriRequest, ResponseHandler)}.
	 * 
	 * @param request
	 *            the HTTP request
	 * @param handler
	 *            the response handler
	 * @return a {@code Future} wrapping the return value from the response
	 *         handler.
	 */
	public <T> Future<T> submit(final HttpUriRequest request,
			final ResponseHandler<T> handler) {
		return schedule(request, handler, 0, TimeUnit.MICROSECONDS);
	}

	/**
	 * Use to sign an outgoing HTTP request with OAuth, using the relevant
	 * authentication stored in the IdManager
	 * 
	 * @param request
	 *            The request to sign
	 * @param idManager
	 *            The IdManager that handles the authentication information
	 * @throws OAuthSignatureException
	 *             if the request was unable to be signed
	 */
	public static void signRequest(HttpRequestBase request, long userId,
			String secret) throws OAuthSignatureException {
		ServiceFinder
				.setIteratorProvider(new OAuthSignatureMethodServiceProvider<OAuthSignatureMethod>());
		OAuthRequest oauthRequest = new OAuthManesRequest(request);
		OAuthParameters oauthParams = new OAuthParameters()
				.consumerKey(Long.toString(userId)).token(null)
				.signatureMethod(HMAC_SHA1.NAME).timestamp().nonce().version();
		OAuthSecrets oauthSecrets = new OAuthSecrets().consumerSecret(secret)
				.tokenSecret(null);
		OAuthSignature.sign(oauthRequest, oauthParams, oauthSecrets);
	}

	/**
	 * Static method for reading a string from an HttpEntity using an
	 * InputStream reader. The String can then be used / converted to JSON
	 * object, etc.
	 * 
	 * @param entity
	 * @return The String encoded in the HttpEntity
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	static public String ReadResponseEntity(HttpEntity entity)
			throws IOException {
		InputStream inStream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inStream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		String data = sb.toString();
		return data;
	}
}
