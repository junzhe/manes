package org.whispercomm.manes.exp.locationsensor.data;

public class GPS {
	String time;
	double lat;
	double lon;

	public GPS() {
		super();
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public GPS(String time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public boolean isDataTheSame(GPS theOther) {
		if (theOther == null)
			return false;
		if (theOther.getTime().compareTo(this.time) == 0
				&& theOther.getLat() == this.lat
				&& theOther.getLon() == this.lon)
			return true;
		else
			return false;
	}
}
