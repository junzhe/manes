package org.whispercomm.manes.client.macentity.location;

import org.json.JSONException;
import org.json.JSONObject;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.http.LocationRequest;
import org.whispercomm.manes.client.macentity.http.LocationResponseHandler;
import org.whispercomm.manes.client.macentity.http.ManesHttpException;
import org.whispercomm.manes.client.macentity.network.IdManager;
import org.whispercomm.manes.client.macentity.network.NotRegisteredException;

import com.sun.jersey.oauth.signature.OAuthSignatureException;

import android.util.Log;

/**
 * This {@link Runnable} is used to update location sensor measurements to the
 * server.
 * 
 * @author Yue Liu
 */
public class LocationSender {

	public static final String TAG = LocationSender.class.getSimpleName();
	/**
	 * The number of retrying posting location update when we get 300 error code
	 * from server.
	 */
	public static final int MORE_DETAIL_RETRY_NUM = 1;

	private final TopologyServerSynchronizer synchronizer;
	private final HttpManager httpManager;
	private final IdManager idManager;

	public LocationSender(TopologyServerSynchronizer synchronizer,
			HttpManager httpManager, IdManager idManager) {
		this.synchronizer = synchronizer;
		this.httpManager = httpManager;
		this.idManager = idManager;
	}

	/**
	 * Post the current location data to the server.
	 * 
	 * @param retryNum
	 *            the number of retried if we get need-more-detail error code
	 *            from the topology server.
	 */
	public void postLocation(int retryNum) {
		try {
			long userId = idManager.getUserId();
			JSONObject locationUpdate = synchronizer.getLatestReportToServer();
			LocationRequest request = new LocationRequest(userId,
					locationUpdate);
			try {
				HttpManager.signRequest(request, userId,
						idManager.getSharedSecret());
			} catch (OAuthSignatureException e) {
				// This should only happen due to buggy code, so it should be
				// caught
				// in testing.
				Log.e(TAG, "Unable to sign location update with OAuth", e);
			}
			LocationResponseHandler handler = new LocationResponseHandler(
					synchronizer, this, retryNum);
			httpManager.submit(request, handler);
		} catch (ManesHttpException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (NotRegisteredException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
