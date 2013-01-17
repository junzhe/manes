package org.whispercomm.manes.client.macentity.http;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.whispercomm.manes.client.macentity.location.ManesLocationManager;

/**
 * Send Location request.
 *
 * @author Junzhe Zhang
 *
 */
public class LocationRequest extends HttpPost {

    public static final String TAG = "org.whispercomm.manes.client.macentity"
            + ".http.LocationRequest";
    private JSONObject locationUpdate;

    public LocationRequest(long userId, JSONObject locationUpdate)
            throws ManesHttpException {
        super(ManesLocationManager.SERVER_URL + "/user/" + userId + "/location/");
        this.locationUpdate = locationUpdate;
        this.prepareRequest();
    }

    private void prepareRequest() throws ManesHttpException {
        try {
            // Convert the JSON object to an HttpEntity and hand it to the
            // request
            this.setHeader("Content-Type", "application/json");
            this.setEntity(new StringEntity(locationUpdate.toString()));
            Log.d("Result", locationUpdate.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getClass().getName());
            throw new ManesHttpException();
        }
    }
}
