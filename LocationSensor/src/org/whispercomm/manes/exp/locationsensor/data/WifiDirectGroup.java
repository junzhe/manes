package org.whispercomm.manes.exp.locationsensor.data;

/*
 * This class describes information about a Wifi group
 */
public class WifiDirectGroup {
	String time;
	String netInterface;
	long netifLong;
	String ssid;

	public WifiDirectGroup() {
		super();
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return this.time;
	}

	public void setNetInterface(String netIf) {
		this.netInterface = netIf;
	}

	public String getNetInterface() {
		return netInterface;
	}

	public void setNetifLong(long netif) {
		this.netifLong = netif;
	}

	public long getNetifLong() {
		return this.netifLong;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getSsid() {
		return ssid;
	}
}
