package org.whispercomm.manes.topology.location;

public class GPS {

	double lat;
	double lon;
	boolean asPrev;

	public GPS() {
		super();
	}

	public GPS(double lat, double lon) {
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

	public void setAsPrev(boolean asPrev) {
		this.asPrev = asPrev;
	}

	public boolean getAsPrev() {
		return this.asPrev;
	}

	@Override
	public String toString() {
		return PojoToString.toString(this);
	}

	/**
	 * Using Vincenty's formulae to calculate distance according to GPS latitude
	 * and longitude.
	 * 
	 * @param gps1
	 * @param gps2
	 * @return
	 */
	public static double getDistance(GPS gps1, GPS gps2) {
		return DistanceVincenty.getGpsDistance(gps1.getLat(), gps1.getLon(),
				gps2.getLat(), gps2.getLon());
	}

	public boolean isDataTheSame(GPS theOther) {
		if (theOther == null)
			return false;
		if (theOther.getLat() == this.lat && theOther.getLon() == this.lon)
			return true;
		else
			return false;
	}
}
