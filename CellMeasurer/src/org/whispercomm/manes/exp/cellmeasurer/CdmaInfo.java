package org.whispercomm.manes.exp.cellmeasurer;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CDMA Information
 * 
 * @author Junzhe Zhang
 */
public class CdmaInfo extends CellInfo {

	static final String TAG = "org.whispercomm.manes.client.macentity"
			+ ".location.CdmaInfo";

	public int sid;
	public int nid;
	public int bid;
	public int sid_server;
	public int nid_server;
	public int bid_server;

	@Override
	synchronized public void updateIsPrev() {
		super.updateIsPrev();
		if (isPrev == false) {
			return;
		} else if (sid_server != sid) {
			isPrev = false;
			return;
		} else if (nid_server != nid) {
			isPrev = false;
			return;
		} else if (bid_server != bid) {
			isPrev = false;
			return;
		}
		isPrev = true;
	}

	@Override
	synchronized public void updateVersion() {
		super.updateVersion();
		sid_server = sid;
		nid_server = nid;
		bid_server = bid;
	}

	@Override
	public JSONObject prepareJSON() {
		JSONObject data = new JSONObject();
		try {
			data.put("mcc", mcc);
			data.put("sid", sid);
			data.put("nid", nid);
			data.put("bid", bid);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return data;
	}
}
