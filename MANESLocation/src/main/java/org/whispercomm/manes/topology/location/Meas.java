package org.whispercomm.manes.topology.location;

public class Meas {
	int freq;
	int rssi;

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	@Override
	public String toString() {
		return PojoToString.toString(this);
	}

	public boolean isDataTheSame(Meas theOther) {
		if (theOther == null)
			return false;
		if (theOther.getFreq() == this.freq && theOther.getRssi() == this.rssi)
			return true;
		else
			return false;
	}
}
