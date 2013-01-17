package org.whispercomm.manes.exp.locationsensor.data;

public class Wifi {
	long ap;
	String ssid;
	Meas meas;
	
	public long getAp() {
		return ap;
	}

	public void setAp(long ap) {
		this.ap = ap;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getSsid() {
		return ssid;
	}
	
	public Meas getMeas() {
		return meas;
	}

	public void setMeas(Meas meas) {
		this.meas = meas;
	}

	public boolean isDataTheSame(Wifi theOther) {
		if (theOther == null)
			return false;
		if (theOther.getAp() == this.ap
				&& theOther.getMeas().isDataTheSame(this.meas))
			return true;
		else
			return false;
	}

	/**
	 * Translate a MAC string into long.
	 * 
	 * @param mac
	 *            the string representation of MAC address.
	 * @return
	 */
	public static long TranslateMacToLong(String mac) {
		String[] numbers = mac.split(":");
		String macClear = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			macClear = macClear.concat(numbers[i]);
		}
		return Long.valueOf(macClear, 16);
	}
}
