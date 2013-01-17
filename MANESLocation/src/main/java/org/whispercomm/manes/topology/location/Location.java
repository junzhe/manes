package org.whispercomm.manes.topology.location;

public class Location {
	Wifis wifi;
	GSM gsm;
	CDMA cdma;
	GPS gps;

	/**
	 * Decide whether any of the sub-fields, i.e., Wifi, GPS, GSM, CDMA, are as
	 * previous.
	 * 
	 * @return
	 */
	public boolean needPrev() {
		boolean needPrev = false;
		if (gps != null)
			needPrev = needPrev || gps.getAsPrev();
		if (gsm != null)
			needPrev = needPrev || gsm.getAsPrev();
		if (cdma != null)
			needPrev = needPrev || cdma.getAsPrev();
		if (wifi != null)
			needPrev = needPrev || wifi.getAsPrev();

		return needPrev;
	}

	/**
	 * Populate all as-previous fields with detailed data.
	 * 
	 * @throws NoPreviousDataException
	 */
	public void populateAsPrev(Location locationLast)
			throws NoPreviousDataException {
		if (gps != null) {
			if (gps.getAsPrev() == true) {
				GPS gpsPrev = locationLast.getGps();
				if (gpsPrev == null)
					throw new NoPreviousDataException("");
				setGps(gpsPrev);
			}
		}
		if (gsm != null) {
			if (gsm.getAsPrev() == true) {
				GSM gsmPrev = locationLast.getGsm();
				if (gsmPrev == null)
					throw new NoPreviousDataException("");
				setGsm(gsmPrev);
			}
		}
		if (cdma != null) {
			if (cdma.getAsPrev() == true) {
				CDMA cdmaPrev = locationLast.getCdma();
				if (cdmaPrev == null)
					throw new NoPreviousDataException("");
				setCdma(cdmaPrev);
			}
		}
		if (wifi != null) {
			if (wifi.getAsPrev() == true) {
				Wifis wifiPrev = locationLast.getWifi();
				if (wifiPrev == null)
					throw new NoPreviousDataException("");
				setWifi(wifiPrev);
			}
		}
	}

	public Wifis getWifi() {
		return wifi;
	}

	public void setWifi(Wifis wifi) {
		this.wifi = wifi;
	}

	public GSM getGsm() {
		return gsm;
	}

	public void setGsm(GSM gsm) {
		this.gsm = gsm;
	}

	public CDMA getCdma() {
		return cdma;
	}

	public void setCdma(CDMA cdma) {
		this.cdma = cdma;
	}

	public GPS getGps() {
		return gps;
	}

	public void setGps(GPS gps) {
		this.gps = gps;
	}

	@Override
	public String toString() {
		return PojoToString.toString(this);
	}

}
