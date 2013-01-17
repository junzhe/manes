package org.whispercomm.manes.client.macentity.http;

/**
 * Handler to handle location response.
 *
 * @author Junzhe Zhang
 * @author Yue Liu
 */
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.whispercomm.manes.client.macentity.location.LocationSender;
import org.whispercomm.manes.client.macentity.location.TopologyServerSynchronizer;

public class LocationResponseHandler implements ResponseHandler<Integer> {

	public static final String TAG = "org.whispercomm.manes.client.macentity."
			+ "http.LocationResponseHandler";
	private final TopologyServerSynchronizer synchronizer;
	private final LocationSender locationUpdater;
	private int retryNum;

	public LocationResponseHandler(TopologyServerSynchronizer synchronizer,
			LocationSender locationUpdater, int retryNum) {
		this.synchronizer = synchronizer;
		this.locationUpdater = locationUpdater;
		this.retryNum = retryNum;
	}

	@Override
	public Integer handleResponse(HttpResponse response){
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 201) {
			Log.i(TAG, "Successfully updated location information.");
			synchronizer.syncServerRecord();
		} else {
			synchronizer.unSyncServerRecord();
			if (statusCode == 300 && retryNum > 0) {
				Log.i(TAG, "Server requests for more detailed location update.");
				locationUpdater.postLocation(retryNum - 1);
			}
		}
		return statusCode;
	}
}
