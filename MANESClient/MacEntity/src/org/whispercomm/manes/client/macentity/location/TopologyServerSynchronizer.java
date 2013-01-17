package org.whispercomm.manes.client.macentity.location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.whispercomm.manes.client.macentity.location.sensor.CdmaSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GsmSensor;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;
import org.whispercomm.manes.topology.location.*;

/**
 * This class is in charge of crafting appropriate JSON object sent to the
 * server. Specifically, it keeps our synchronization state and would avoid
 * sending repetitive content to the server.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 */
public class TopologyServerSynchronizer {

	/**
	 * The latest location sensor measurements stored at the server.
	 */
	private Location locationServer;
	/**
	 * The latest location update sent to the server.
	 */
	private Location locationClient;
	private boolean isSynced;
	private final CdmaSensor cdmaSensor;
	private final GsmSensor gsmSensor;
	private final GpsSensor gpsSensor;
	private final WifiSensor wifiSensor;

	public TopologyServerSynchronizer(CdmaSensor cdmaSensor,
			GsmSensor gsmSensor, GpsSensor gpsSensor, WifiSensor wifiSensor) {
		this.locationServer = new Location();
		this.isSynced = false;
		this.cdmaSensor = cdmaSensor;
		this.gsmSensor = gsmSensor;
		this.gpsSensor = gpsSensor;
		this.wifiSensor = wifiSensor;
	}

	/**
	 * Return the last location update stored in the topology server.
	 * 
	 * @return
	 * @throws ServerUnSyncedException
	 */
	public Location getLatestServerRecord() throws ServerUnSyncedException {
		if (isSynced == false)
			throw new ServerUnSyncedException();
		return locationServer;
	}

	/**
	 * Update the record of the latest sensor measurements stored in the server.
	 */
	public void syncServerRecord() {
		locationServer = locationClient;
		isSynced = true;
	}

	/**
	 * Mark that we have lost synchronization with the server.
	 */
	public void unSyncServerRecord() {
		isSynced = false;
	}

	private void getLatestSensorMeasurement() {
		CDMA cdma = cdmaSensor.getLatestReading();
		GSM gsm = gsmSensor.getLatestReading();
		GPS gps = gpsSensor.getLatestReading();
		Wifis wifi = wifiSensor.getLatestReading();
		locationClient = new Location();
		locationClient.setCdma(cdma);
		locationClient.setGps(gps);
		locationClient.setGsm(gsm);
		locationClient.setWifi(wifi);
	}

	/**
	 * Return the latest sensor measurement to the server. Make sure the
	 * as-previous fields are correctly populated according our sync condition.
	 * 
	 * @return an object that represents the sensor measurements.
	 */
	public JSONObject getLatestReportToServer() throws JSONException {
		// always get the latest sensor readings first.
		getLatestSensorMeasurement();
		JSONObject locationUpdate = new JSONObject();
		locationUpdate.put("wifi", getLatestWifiToServer());
		locationUpdate.put("cdma", getLatestCdmaToServer());
		locationUpdate.put("gsm", getLatestGsmToServer());
		locationUpdate.put("gps", getLatestGpsToServer());
		return locationUpdate;
	}

	private JSONObject getLatestCdmaToServer() throws JSONException {
		CDMA cdma = locationClient.getCdma();
		if (cdma == null) {
			return null;
		}
		// set as-previous properly
		if (isSynced) {
			cdma.setAsPrev(cdma.isDataTheSame(locationServer.getCdma()));
		} else {
			cdma.setAsPrev(false);
		}
		// prepare the JSONObject
		JSONObject data = new JSONObject();
		boolean asPrev = cdma.getAsPrev();
		data.put("asPrev", asPrev);
		if (asPrev) {
			return data;
		} else {
			data.put("mcc", cdma.getMcc());
			data.put("sid", cdma.getSid());
			data.put("nid", cdma.getNid());
			data.put("bid", cdma.getBid());
			return data;
		}
	}

	private JSONObject getLatestGsmToServer() throws JSONException {
		GSM gsm = locationClient.getGsm();
		if (gsm == null) {
			return null;
		}
		// set as-previous properly
		if (isSynced) {
			gsm.setAsPrev(gsm.isDataTheSame(locationServer.getGsm()));
		} else {
			gsm.setAsPrev(false);
		}
		boolean asPrev = gsm.getAsPrev();
		JSONObject data = new JSONObject();
		data.put("asPrev", asPrev);
		if (asPrev == true) {
			return data;
		} else {
			data.put("mcc", gsm.getMcc());
			data.put("mnc", gsm.getMnc());
			data.put("lac", gsm.getLac());
			data.put("cid", gsm.getCid());
			return data;
		}
	}

	private JSONObject getLatestGpsToServer() throws JSONException {
		GPS gps = locationClient.getGps();
		if (gps == null) {
			return null;
		}
		// set as-previous properly
		if (isSynced) {
			gps.setAsPrev(gps.isDataTheSame(locationServer.getGps()));
		} else {
			gps.setAsPrev(false);
		}
		boolean asPrev = gps.getAsPrev();
		JSONObject data = new JSONObject();
		data.put("asPrev", asPrev);
		if (asPrev == true) {
			return data;
		} else {
			data.put("lat", gps.getLat());
			data.put("lon", gps.getLon());
			return data;
		}
	}

	private JSONObject getLatestWifiToServer() throws JSONException {
		Wifis wifis = locationClient.getWifi();
		if (wifis == null) {
			return null;
		}
		if (wifis.getWifi() == null) {
			return null;
		}
		// set as-previous properly
		if (isSynced) {
			wifis.setAsPrev(wifis.isDataTheSame(locationServer.getWifi()));
		} else {
			wifis.setAsPrev(false);
		}
		boolean asPrev = wifis.getAsPrev();
		JSONObject data = new JSONObject();
		data.put("asPrev", asPrev);
		if (asPrev == true) {
			return data;
		}
		JSONArray wifi = new JSONArray();
		for (Wifi result : wifis.getWifi()) {
			JSONObject sub = new JSONObject();
			sub.put("ap", result.getAp());
			JSONObject meas = new JSONObject();
			meas.put("freq", result.getMeas().getFreq());
			meas.put("rssi", result.getMeas().getRssi());
			sub.put("meas", meas);
			wifi.put(sub);
		}
		data.put("wifi", wifi);
		return data;
	}
}
