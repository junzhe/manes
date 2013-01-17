package org.whispercomm.manes.exp.locationsensor.data;

import java.util.List;

public class Wifis {
	String time;
	List<Wifi> wifi;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setWifi(List<Wifi> wifi) {
		this.wifi = wifi;
	}

	public List<Wifi> getWifi() {
		return wifi;
	}
}
