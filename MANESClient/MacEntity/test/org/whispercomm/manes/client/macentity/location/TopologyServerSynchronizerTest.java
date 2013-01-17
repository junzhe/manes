package org.whispercomm.manes.client.macentity.location;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.location.sensor.CdmaSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.location.sensor.GsmSensor;
import org.whispercomm.manes.client.macentity.location.sensor.WifiSensor;
import org.whispercomm.manes.topology.location.CDMA;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GSM;
import org.whispercomm.manes.topology.location.Meas;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TopologyServerSynchronizerTest {

	private TopologyServerSynchronizer synchronizer;
	private CDMA cdma;
	private GSM gsm;
	private GPS gps;
	private Wifis wifi;

	public void setUp() {
		// prepare cdma sensor
		cdma = new CDMA();
		cdma.setBid(0);
		cdma.setMcc("asdf");
		cdma.setNid(1);
		cdma.setSid(2);
		CdmaSensor cdmaSensor = mock(CdmaSensor.class);
		when(cdmaSensor.getLatestReading()).thenReturn(cdma);
		// prepare gsm sensor
		gsm = new GSM();
		gsm.setCid(0);
		gsm.setLac(1);
		gsm.setMcc("asfdad");
		gsm.setMnc(2);
		GsmSensor gsmSensor = mock(GsmSensor.class);
		when(gsmSensor.getLatestReading()).thenReturn(gsm);
		// prepare gps sensor
		gps = new GPS(-1, -1);
		GpsSensor gpsSensor = mock(GpsSensor.class);
		when(gpsSensor.getLatestReading()).thenReturn(gps);
		// prepare Wifi sensor
		wifi = new Wifis();
		List<Wifi> wifiList = new LinkedList<Wifi>();
		Wifi wifiInList = new Wifi();
		wifiInList.setAp(0);
		Meas meas = new Meas();
		meas.setFreq(2);
		meas.setRssi(-90);
		wifiInList.setMeas(meas);
		wifiList.add(wifiInList);
		wifi.setWifi(wifiList);
		WifiSensor wifiSensor = mock(WifiSensor.class);
		when(wifiSensor.getLatestReading()).thenReturn(wifi);
		synchronizer = new TopologyServerSynchronizer(cdmaSensor, gsmSensor,
				gpsSensor, wifiSensor);
	}

	@Test
	public void testSyncAndReport() throws JSONException {
		setUp();
		// get first report
		JSONObject report = synchronizer.getLatestReportToServer();
		JSONObject wifi = (JSONObject) report.get("wifi");
		assertTrue(wifi.getBoolean("asPrev") == false);
		JSONArray wifis = null;
		try {
			wifis = wifi.getJSONArray("wifi");
		} catch (JSONException e1) {
			fail();
		}
		assertTrue(wifis.length() == 1);
		JSONObject wifiInList = wifis.getJSONObject(0);
		assertTrue(wifiInList.getLong("ap") == 0);
		assertTrue(((JSONObject) wifiInList.get("meas")).getInt("freq") == 2);
		assertTrue(((JSONObject) wifiInList.get("meas")).getInt("rssi") == -90);
		synchronizer.syncServerRecord();
		// get second report
		// get first report
		report = synchronizer.getLatestReportToServer();
		wifi = (JSONObject) report.get("wifi");
		assertTrue(wifi.getBoolean("asPrev") == true);
		try {
			wifis = wifi.getJSONArray("wifi");
			fail("Should not contain any previous data!");
		} catch (JSONException e) {
		}
		// get report after unsync
		synchronizer.unSyncServerRecord();
		report = synchronizer.getLatestReportToServer();
		wifi = (JSONObject) report.get("wifi");
		assertTrue(wifi.getBoolean("asPrev") == false);
		wifis = null;
		try {
			wifis = wifi.getJSONArray("wifi");
		} catch (JSONException e1) {
			fail();
		}
		assertTrue(wifis.length() == 1);
		wifiInList = wifis.getJSONObject(0);
		assertTrue(wifiInList.getLong("ap") == 0);
		assertTrue(((JSONObject) wifiInList.get("meas")).getInt("freq") == 2);
		assertTrue(((JSONObject) wifiInList.get("meas")).getInt("rssi") == -90);
	}

}
