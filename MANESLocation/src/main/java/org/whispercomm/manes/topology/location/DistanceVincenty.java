package org.whispercomm.manes.topology.location;

public class DistanceVincenty {

	public static double A_WGS84 = 6378137;
	public static double B_WGS84 = 6356752.314245;
	public static double F_WGS84 = 1 / 298.257223563; // WGS-84 ellipsoid params

	/**
	 * From degree to radian.
	 * 
	 * @param degree
	 * @return
	 */
	private static double toRad(double degree) {
		return Math.PI * degree / 180;
	}

	/**
	 * Use Vincenty formula to calculate distances between two GPS coordinates.
	 * We are using WGS-84 ellipsoid model here.
	 * <p>
	 * The code is a literal rewrite from the javascript on this URL
	 * (http://www.movable-type.co.uk/scripts/latlong-vincenty.html).
	 * 
	 * @param lat1
	 *            latitude of the first point in decimal degrees
	 * @param lon1
	 *            longitude of the first point in decimal degrees
	 * @param lat2
	 *            latitude of the second point in decimal degrees
	 * @param lon2
	 *            longitude of the second point in decimal degrees
	 * @return ditance in meters between the two points.
	 */
	public static double getGpsDistance(double lat1, double lon1, double lat2,
			double lon2) {

		double L = toRad(lon2 - lon1);
		double U1 = Math.atan((1 - F_WGS84) * Math.tan(toRad(lat1)));
		double U2 = Math.atan((1 - F_WGS84) * Math.tan(toRad(lat2)));
		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L;
		double lambdaP;
		double cosSqAlpha;
		double sinSigma;
		double cosSigma;
		double cos2SigmaM;
		double sigma;
		int iterLimit = 100;
		while (true) {
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
					+ (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
					* (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
			if (sinSigma == 0)
				return 0; // co-incident points
			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha * sinAlpha;
			cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
			if (Double.valueOf(cos2SigmaM).isNaN())
				cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
			double C = F_WGS84 / 16 * cosSqAlpha
					* (4 + F_WGS84 * (4 - 3 * cosSqAlpha));
			lambdaP = lambda;
			lambda = L
					+ (1 - C)
					* F_WGS84
					* sinAlpha
					* (sigma + C
							* sinSigma
							* (cos2SigmaM + C * cosSigma
									* (-1 + 2 * cos2SigmaM * cos2SigmaM)));
			if (Math.abs(lambda - lambdaP) < 1e-12 || --iterLimit < 0)
				break;
		}

		if (iterLimit == 0)
			return Double.NaN; // formula failed to converge

		double uSq = cosSqAlpha * (A_WGS84 * A_WGS84 - B_WGS84 * B_WGS84)
				/ (B_WGS84 * B_WGS84);
		double A = 1 + uSq / 16384
				* (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
		double deltaSigma = B
				* sinSigma
				* (cos2SigmaM + B
						/ 4
						* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B
								/ 6 * cos2SigmaM
								* (-3 + 4 * sinSigma * sinSigma)
								* (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		double s = B_WGS84 * A * (sigma - deltaSigma);

		// s = s.toFixed(3); // round to 1mm precision
		return s;
	}
}
