package org.whispercomm.manes.client.macentity.location.operator;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.location.operator.WifiOperator;
import org.whispercomm.manes.topology.location.Meas;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class WifiOperatorTest {

	@Test
	public void testHasSignificantWifiChange() {
		// wifi1
		Wifis wifi1 = new Wifis();
		List<Wifi> wifiList = new LinkedList<Wifi>();
		Wifi wifiInList = new Wifi();
		wifiInList.setAp(0);
		Meas meas = new Meas();
		meas.setFreq(2);
		meas.setRssi(-90);
		wifiInList.setMeas(meas);
		wifiList.add(wifiInList);
		wifi1.setWifi(wifiList);
		assertFalse(WifiOperatorPolicy.hasSignificantWifiChange(wifi1, wifi1));
		// wifi2
		Wifis wifi2 = new Wifis();
		List<Wifi> wifiList2 = new LinkedList<Wifi>();
		for (int i = 0; i < 5; i++) {
			Wifi wifiInListCrt = new Wifi();
			wifiInList.setAp(i);
			Meas measCrt = new Meas();
			measCrt.setFreq(2);
			measCrt.setRssi(-90);
			wifiInListCrt.setMeas(measCrt);
			wifiList2.add(wifiInListCrt);
		}
		wifi2.setWifi(wifiList2);
		assertTrue(WifiOperator.hasSignificantWifiChange(wifi1, wifi2));
		// wifi3
		Wifis wifi3 = new Wifis();
		List<Wifi> wifiList3 = new LinkedList<Wifi>();
		Wifi wifiInList3 = new Wifi();
		wifiInList.setAp(0);
		Meas meas3 = new Meas();
		meas3.setFreq(2);
		meas3.setRssi(-80);
		wifiInList3.setMeas(meas3);
		wifiList3.add(wifiInList3);
		wifi3.setWifi(wifiList3);
		assertTrue(WifiOperator.hasSignificantWifiChange(wifi1, wifi3));
	}

}
