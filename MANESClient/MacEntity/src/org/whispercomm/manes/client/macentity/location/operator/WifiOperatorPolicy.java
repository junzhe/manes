package org.whispercomm.manes.client.macentity.location.operator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.whispercomm.manes.topology.location.Meas;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

/**
 * This class contains various logics related to Wifi scan results.
 * 
 * @author Yue Liu
 * 
 */
public class WifiOperatorPolicy {

	public static final String TAG = WifiOperatorPolicy.class.getSimpleName();
	private static final int AP_NUM_DIFF_THREASH = 3;
	private static final int RSSI_DIFF_THRESH = 3;
	private static final double COMMON_AP_RATIO_THRESH = 0.5;
	public static final int RSSI_STRONG = -75;

	/**
	 * Decide whether there are significant changes in Wifi measurement results.
	 * 
	 * @param wifis1
	 * @param wifis2
	 * @return
	 */
	public static boolean hasSignificantWifiChange(Wifis wifis1, Wifis wifis2) {
		List<Wifi> wifi1 = wifis1.getWifi();
		List<Wifi> wifi2 = wifis2.getWifi();
		List<Integer> rssis1 = new LinkedList<Integer>();
		List<Integer> rssis2 = new LinkedList<Integer>();
		Set<Long> commonAps = getWifiIntersection(wifi1, wifi2, rssis1, rssis2);
		int commonApNum = commonAps.size();
//		Log.i(TAG, "***commonApNum: " + commonApNum);
		int oldNum = wifi1.size();
		int newNum = wifi2.size();
		if (commonApNum == 0) {
			if (oldNum != 0 || newNum != 0)
				return true;
		} else {
			double commonRatioOld = ((double) commonApNum) / ((double) oldNum);
			double commonRatioNew = ((double) commonApNum) / ((double) newNum);
			int diff = getAvgRssiDiffs(rssis1, rssis2);
//			Log.i(TAG, "***commonRatioOld: " + commonRatioOld);
//			Log.i(TAG, "***commonRatioNew: " + commonRatioNew);
//			Log.i(TAG, "***Avg. RSSI diff: " + diff);
			if ((Math.abs(commonApNum - oldNum) >= AP_NUM_DIFF_THREASH && commonRatioOld < 0.7)
					|| (Math.abs(commonApNum - wifi2.size()) >= AP_NUM_DIFF_THREASH && commonRatioNew < 0.7)
					|| diff > RSSI_DIFF_THRESH) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decide whether our Wifi environment has changed enough to justify a new
	 * GPS measurement.
	 * <p>
	 * This requires more change than {@code hasSignificantWifiChange}.
	 * 
	 * @param wifis1
	 *            old Wifi measurements.
	 * @param wifis2
	 *            new Wifi measurements.
	 * @return
	 */
	public static boolean hasWifiEnvironmentChanged(Wifis wifis1, Wifis wifis2) {
		List<Wifi> wifi1 = wifis1.getWifi();
		List<Wifi> wifi2 = wifis2.getWifi();
		List<Integer> rssis1 = new LinkedList<Integer>();
		List<Integer> rssis2 = new LinkedList<Integer>();
		Set<Long> commonAps = getWifiIntersection(wifi1, wifi2, rssis1, rssis2);
		int commonApNum = commonAps.size();
		int rssiDiff = 0;
		if (commonApNum > 0)
			rssiDiff = getAvgRssiDiffs(rssis1, rssis2);
		int oldNum = wifi1.size();
		int newNum = wifi2.size();
		if (oldNum == 0) {
			if (newNum == 0)
				return false;
			else
				return true;
		} else {
			double commonRatio = ((double) commonApNum) / ((double) oldNum);
			if (commonRatio < COMMON_AP_RATIO_THRESH
					|| rssiDiff > RSSI_DIFF_THRESH)
				// Either a significant portion of previous measured APs have
				// disappeared or the RSSIs of common APs changed significantly,
				// we decide the Wifi environment has changed significantly.
				return true;
			else
				return false;
		}
	}

	/**
	 * Whether we get strong Wifi signal.
	 * 
	 * @param wifiReading
	 * @return true if all the readings are above the threshold
	 *         {@code RSSI_STRONG}.
	 */
	public static boolean isWifiStrong(Wifis wifiReading) {
		boolean isStrong = false;
		if (wifiReading == null) {
			return false;
		}
		List<Wifi> wifis = wifiReading.getWifi();
		if (wifis == null) {
			return false;
		}
		Iterator<Wifi> it = wifis.iterator();
		while (it.hasNext()) {
			if (it.next().getMeas().getRssi() < RSSI_STRONG) {
				isStrong = false;
			} else {
				isStrong = true;
				break;
			}
		}
		return isStrong;
	}

	/**
	 * Get the common APs of two Wifi measurements, and their corresponding
	 * RSSIs in each measurement.
	 * 
	 * @param wifi1
	 * @param wifi2
	 * @param rssis1
	 * @param rssis2
	 * @return
	 */
	private static Set<Long> getWifiIntersection(List<Wifi> wifi1,
			List<Wifi> wifi2, List<Integer> rssis1, List<Integer> rssis2) {
		Map<Long, Meas> wifiMap1 = getAPMeasures(wifi1);
		Map<Long, Meas> wifiMap2 = getAPMeasures(wifi2);
		Set<Long> aps1 = wifiMap1.keySet();
		Set<Long> aps2 = wifiMap2.keySet();
		// get the intersection of two AP sets
		aps1.retainAll(aps2);
		Iterator<Long> it = aps1.iterator();
		Long ap;
		while (it.hasNext()) {
			ap = it.next();
			rssis1.add(wifiMap1.get(ap).getRssi());
			rssis2.add(wifiMap2.get(ap).getRssi());
		}
		return aps1;
	}

	/**
	 * Transform {@code List<Wifi>} structure into a {@link Map}.
	 * 
	 * @param wifi
	 * @return
	 */
	private static Map<Long, Meas> getAPMeasures(List<Wifi> wifi) {
		Map<Long, Meas> map = new HashMap<Long, Meas>();
		if (wifi == null)
			return map;
		Iterator<Wifi> it = wifi.iterator();
		Wifi wifiCrt;
		while (it.hasNext()) {
			wifiCrt = it.next();
			map.put(wifiCrt.getAp(), wifiCrt.getMeas());
		}
		return map;
	}

	/**
	 * Get the average RSSI difference of the common APs in two Wifi
	 * measurements.
	 * 
	 * @param rssis1
	 * @param rssis2
	 * @return
	 */
	private static int getAvgRssiDiffs(List<Integer> rssis1,
			List<Integer> rssis2) {
		int size = rssis1.size();
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum += Math.abs(rssis1.get(i) - rssis2.get(i));
		}
		return sum / size;
	}

}
