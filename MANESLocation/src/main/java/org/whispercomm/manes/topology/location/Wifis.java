package org.whispercomm.manes.topology.location;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Wifis {

	List<Wifi> wifi;
	boolean asPrev;

	public void setWifi(List<Wifi> wifi) {
		this.wifi = wifi;
	}

	public List<Wifi> getWifi() {
		return wifi;
	}

	public void setAsPrev(boolean asPrev) {
		this.asPrev = asPrev;
	}

	public boolean getAsPrev() {
		return this.asPrev;
	}

	public boolean isDataTheSame(Wifis theOther) {
		if (theOther == null)
			return false;
		List<Wifi> wifis2 = theOther.getWifi();
		if (wifis2 == null) {
			if (this.wifi == null)
				return true;
			else
				return false;
		}
		if (this.wifi == null) return false;
		if (this.wifi.size() != wifis2.size())
			return false;
		// now compare the record one-by-one
		Map<Long, Wifi> map = toWifiMap(this.wifi);
		Map<Long, Wifi> map2 = toWifiMap(wifis2);
		Iterator<Long> it = map.keySet().iterator();
		Long apCrt;
		Wifi wifiCrt;
		Wifi wifiCrt2;
		while (it.hasNext()) {
			apCrt = it.next();
			wifiCrt = map.get(apCrt);
			wifiCrt2 = map2.get(apCrt);
			if (wifiCrt2 == null)
				return false;
			if (wifiCrt.isDataTheSame(wifiCrt2))
				map2.remove(apCrt);
			else
				return false;
		}
		if (map2.size() == 0)
			return true;
		else
			return false;
	}

	protected Map<Long, Wifi> toWifiMap(List<Wifi> wifis) {
		Map<Long, Wifi> map = new HashMap<Long, Wifi>();
		Iterator<Wifi> it = wifis.iterator();
		Long apCrt;
		Wifi wifiCrt;
		while (it.hasNext()) {
			wifiCrt = it.next();
			apCrt = wifiCrt.getAp();
			map.put(apCrt, wifiCrt);
		}
		return map;
	}
}
