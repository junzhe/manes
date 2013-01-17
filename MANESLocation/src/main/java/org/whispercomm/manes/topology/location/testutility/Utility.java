package org.whispercomm.manes.topology.location.testutility;

import java.util.LinkedList;
import java.util.List;

import org.whispercomm.manes.topology.location.CDMA;
import org.whispercomm.manes.topology.location.GPS;
import org.whispercomm.manes.topology.location.GSM;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Meas;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

public class Utility {

	public static Location initLocationInstance() {

		Location location = new Location();
		location.setWifi(initWifisInstance(false));
		location.setGsm(initGSMInstance(false));
		location.setCdma(initCDMAInstance(false));
		location.setGps(initGPSInstance(false));
		return location;
	}
	
	public static Location initLocationInstanceWifiNull(){
		Location location = new Location();
		location.setGsm(initGSMInstance(false));
		location.setCdma(initCDMAInstance(false));
		location.setGps(initGPSInstance(false));
		return location;
	}
	
	public static Location initLocationInstanceGPSNull(){
		Location location = new Location();
		location.setWifi(initWifisInstance(false));
		location.setGsm(initGSMInstance(false));
		location.setCdma(initCDMAInstance(false));
		return location;
	}
	
	public static Location iniLocationInstanceAsPrev(){
		Location location = new Location();
		location.setWifi(initWifisInstanceAsPrev());
		location.setGsm(initGSMInstanceAsPrev());
		location.setCdma(initCDMAInstanceAsPrev());
		location.setGps(initGPSInstanceAsPrev());
		return location;
	}

	public static Wifis initWifisInstance(boolean asPrev) {
		Wifis wifis = new Wifis();
		List<Wifi> wifiList = new LinkedList<Wifi>();
		wifiList.add(initWifiInstance());
		wifis.setWifi(wifiList);
		wifis.setAsPrev(asPrev);
		return wifis;
	}
	
	public static Wifis initWifisInstanceAsPrev() {
		Wifis wifis = new Wifis();
		List<Wifi> wifiList = null;
		wifis.setWifi(wifiList);
		wifis.setAsPrev(true);
		return wifis;
	}

	public static Wifi initWifiInstance() {
		Wifi wifi = new Wifi();
		wifi.setAp(111);
		wifi.setMeas(initMeasInstance());
		return wifi;
	}

	public static Meas initMeasInstance() {
		Meas meas = new Meas();
		meas.setFreq(123);
		meas.setRssi(-83);
		return meas;
	}

	public static GSM initGSMInstance(boolean asPrev) {
		GSM gsm = new GSM();
		gsm.setAsPrev(asPrev);
		gsm.setCid(1);
		gsm.setLac(2);
		gsm.setMcc("US");
		gsm.setMnc(4);
		return gsm;
	}
	
	public static GSM initGSMInstanceAsPrev() {
		GSM gsm = new GSM();
		gsm.setAsPrev(true);
		return gsm;
	}

	public static CDMA initCDMAInstance(boolean asPrev) {
		CDMA cdma = new CDMA();
		cdma.setAsPrev(asPrev);
		cdma.setBid(4);
		cdma.setMcc("US");
		cdma.setNid(6);
		cdma.setSid(7);
		return cdma;
	}
	
	public static CDMA initCDMAInstanceAsPrev() {
		CDMA cdma = new CDMA();
		cdma.setAsPrev(true);
		return cdma;
	}

	public static GPS initGPSInstance(boolean asPrev) {
		GPS gps = new GPS();
		gps.setAsPrev(asPrev);
		gps.setLat(55.55);
		gps.setLon(66.66);
		return gps;
	}
	
	public static GPS initGPSInstanceAsPrev() {
		GPS gps = new GPS();
		gps.setAsPrev(true);
		return gps;
	}
}
