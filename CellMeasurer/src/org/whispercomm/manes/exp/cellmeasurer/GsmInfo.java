package org.whispercomm.manes.exp.cellmeasurer;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GSM Infomation
 * 
 * @author Junzhe Zhang
 */
public class GsmInfo extends CellInfo {

	public static final String TAG = "org.whispercomm.manes.client"
			+ ".macentity.location.GsmInfo";
	public int mnc;
	public int lac;
	public int cid;
	public int mnc_server;
	public int lac_server;
	public int cid_server;

	@Override
	synchronized public void updateIsPrev() {
		super.updateIsPrev();
		if (isPrev == false) {
			return;
		} else if (mnc_server != mnc) {
			isPrev = false;
			return;
		} else if (lac_server != lac) {
			isPrev = false;
			return;
		} else if (cid_server != cid) {
			isPrev = false;
			return;
		}
		isPrev = true;
	}

	@Override
	synchronized public void updateVersion() {
		super.updateVersion();
		mnc_server = mnc;
		lac_server = lac;
		cid_server = cid;
	}

	public JSONObject prepareJSON() {
		JSONObject data = new JSONObject();
		try {
			data.put("mcc", mcc);
			data.put("mnc", mnc);
			data.put("lac", lac);
			data.put("cid", cid);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return data;
	}
}
