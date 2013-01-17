package org.whispercomm.manes.topology.server.linkestimator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

/**
 * Estimate two clients' link quality according to their measurements of common
 * WiFi access points.
 * 
 * @author Yue Liu
 * 
 */
// TODO add implementation of link estimation based on SNR.
public class WifiPairwiseLinkEstimator implements PairwiseLinkEstimator {

	static final double PATH_LOSS_EXPONENT = 2.5;
	static final double Mu_RSSI = -98;
	static final double Lambda_RSSI = 3.121;
	static final double K_RSSI = 0.6736;

	public final static int RSSI_NO_COMMON_AP = -10000;

	@Override
	public float getLinkQuality(Location client1, Location client2) {
		if (client1 == null || client2 == null)
			return LINK_UNESTIMATABLE;
		// get wifi information
		Wifis wifis1 = client1.getWifi();
		Wifis wifis2 = client2.getWifi();
		// return LINK_UNESTIMATABLE if any of the client do not contain useful
		// wifi scan results.
		if (wifis1 == null || wifis2 == null)
			return LINK_UNESTIMATABLE;
		if (wifis1.getWifi() == null || wifis2.getWifi() == null)
			return LINK_UNESTIMATABLE;
		if (wifis1.getWifi().size() == 0 || wifis2.getWifi().size() == 0)
			return LINK_UNESTIMATABLE;
		// Estimate RSSI
		int rssi = estimateRSSI(wifis1, wifis2);
		if (rssi == RSSI_NO_COMMON_AP)
			return 0;

		// Predict PRR according to RSSI
		float prr = getPRRWithWeibullCurve(rssi, Mu_RSSI, Lambda_RSSI, K_RSSI);
		return prr;
	}

	/**
	 * Estimate a tranceiver's RSSI according to their measurements of a set of
	 * common Access Points (APs).
	 * 
	 * @param client1
	 * @param client2
	 * @return
	 */
	public int estimateRSSI(Wifis client1, Wifis client2) {

		Map<Long, Integer> APRssis1 = getAPRssis(client1);
		Map<Long, Integer> APRssis2 = getAPRssis(client2);

		// get common APs
		Set<Long> commonAPs;
		Set<Long> APs1 = APRssis1.keySet();
		Set<Long> APs2 = APRssis2.keySet();
		if (APs1 == null || APs2 == null)
			commonAPs = new HashSet<Long>();
		else {
			commonAPs = APs1;
			commonAPs.retainAll(APs2);
		}

		// find the largest RSSI estimation among all APs
		int rssiMax = RSSI_NO_COMMON_AP;
		Iterator<Long> it = commonAPs.iterator();
		Long APCrt;
		int rssiCrt;
		while (it.hasNext()) {
			APCrt = it.next();
			rssiCrt = predictRSSIAccordingToAP(APRssis1.get(APCrt),
					APRssis2.get(APCrt), PATH_LOSS_EXPONENT);
			if (rssiCrt > rssiMax)
				rssiMax = rssiCrt;
		}

		return rssiMax;
	}

	/**
	 * Get a client's observed APs and their corresponding RSSIs in a
	 * {@link Map}.
	 * 
	 * @param wifis
	 * @return
	 */
	private Map<Long, Integer> getAPRssis(Wifis wifis) {
		Map<Long, Integer> APRssis = new HashMap<Long, Integer>();
		List<Wifi> wifiList = wifis.getWifi();
		if (wifiList != null) {
			Iterator<Wifi> it = wifiList.listIterator();
			Wifi wifiCrt;
			while (it.hasNext()) {
				wifiCrt = it.next();
				APRssis.put(Long.valueOf(wifiCrt.getAp()),
						Integer.valueOf(wifiCrt.getMeas().getRssi()));
			}
		}
		return APRssis;
	}

	/**
	 * Predict a transceiver pair's RSSI according to their measurement of a
	 * common AP.
	 * 
	 * @param ApRssi1
	 * @param ApRssi2
	 * @param pathLossExponent
	 * @return
	 */
	private int predictRSSIAccordingToAP(int ApRssi1, int ApRssi2,
			double pathLossExponent) {
		return (int) ((-10 * pathLossExponent) * Math.log10(Math.abs(Math.pow(
				10, (-ApRssi1) / (10 * pathLossExponent))
				+ Math.pow(10, (-ApRssi2) / (10 * pathLossExponent)))));
	}

	/**
	 * Predict PRR (Packer Receiption Rate) based on a Weibull curve.
	 * 
	 * @param x
	 *            RSSI or SNR
	 * @param mu
	 * @param lambda
	 * @param k
	 * @return
	 */
	private float getPRRWithWeibullCurve(double x, double mu, double lambda,
			double k) {
		if (x < mu) {
			return 0;
		} else {
			return (float) (1 - Math.exp(-Math.pow((x - mu) / lambda, k)));
		}
	}

}
