package org.whispercomm.manes.topology.location;


/**
 * A grid corresponds to a square area on the surface of Earth. This class is
 * used in a grid-based algorithm to calculate pairwise link quality based on
 * distance.
 * 
 * @author Yue Liu
 * @author Junzhe Zhang
 * 
 */
public class GpsGridElement {

	/**
	 * The smallest distance in meters that two clients cannot hear each other.
	 */
	public static final double DISTANCE_NO_SIGNAL = 250;
	/**
	 * Spherical earth radius.
	 */
	public static final double SPHERICAL_EARTH_RADIUS = 6372800;
	public static final double ONE_RAD_IN_DEGREE = 180 / Math.PI;
	/**
	 * Step-size of latitude.
	 */
	public static final double LAT_STEP_SIZE = DISTANCE_NO_SIGNAL
			/ SPHERICAL_EARTH_RADIUS * ONE_RAD_IN_DEGREE;
	/**
	 * The maximum latitude index, corresponding to the north pole.
	 */
	public static final int LAT_INDEX_MAX = (int) Math.floor(90 / LAT_STEP_SIZE);
	/**
	 * The mininum latitude index, corresponding to the south pole.
	 */
	public static final int LAT_INDEX_MIN = (int) Math.floor(-90 / LAT_STEP_SIZE);

	private int latIndex;
	private int lonIndex;

	public GpsGridElement(GPS gps) {
		double latitude = gps.getLat();
		double longitude = gps.getLon();
		this.latIndex = (int) Math.floor(latitude / LAT_STEP_SIZE);
		double lonStepSize = getLonStepSize(latIndex);
		this.lonIndex = (int) Math.floor(longitude / lonStepSize);
	}

	/**
	 * Get the longitude step size for a given latitude circle.
	 * 
	 * @param latIndex
	 * @return
	 */
	public static double getLonStepSize(int latIndex) {
		double latitude = latIndex * LAT_STEP_SIZE;
		double latInRadian = latitude / ONE_RAD_IN_DEGREE;
		double latCircleRadius = SPHERICAL_EARTH_RADIUS * Math.cos(latInRadian);
		double lonStepSize = DISTANCE_NO_SIGNAL / latCircleRadius
				* ONE_RAD_IN_DEGREE;
		return lonStepSize;
	}

	/**
	 * Get the total number of longitude steps on a given latitude circle.
	 * 
	 * @param latIndex
	 * @return
	 */
	public static int getLonStepNum(int latIndex) {
		double latitude = latIndex * LAT_STEP_SIZE;
		double latInRadian = latitude / ONE_RAD_IN_DEGREE;
		double latCircleRadius = SPHERICAL_EARTH_RADIUS * Math.cos(latInRadian);
		double lonStepSize = DISTANCE_NO_SIGNAL / latCircleRadius
				* ONE_RAD_IN_DEGREE;
		return (int) Math.ceil(360 / lonStepSize);
	}

	public GpsGridElement(int lacIndex, int lonIndex) {
		this.latIndex = lacIndex;
		this.lonIndex = lonIndex;
	}

	/**
	 * No-arg constructor
	 */
	public GpsGridElement() {
	}

//	public double getLatStepSize(){
//		return this.latStepSize;
//	}
//	
//	public double getLonStepSize(){
//		return this.lonStepSize;
//	}
	
	public int getLatIndex() {
		return this.latIndex;
	}

	public int getLonIndex() {
		return this.lonIndex;
	}

//	public void setLatStepSize(double latStepSize){
//		this.latStepSize = latStepSize;
//	}
//	
//	public void setLonStepSize(double lonStepSize){
//		this.lonStepSize = lonStepSize;
//	}
	
	public void setLatIndex(int lacIndex) {
		this.latIndex = lacIndex;
	}

	public void setLonIndex(int lonIndex) {
		this.lonIndex = lonIndex;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this==obj){
			return true;
		}
		if(obj instanceof GpsGridElement){
			GpsGridElement grid = (GpsGridElement) obj;
			if((latIndex==grid.latIndex)&&(lonIndex==grid.lonIndex)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() { 
	    int hash = 1;
	    hash = hash * 31 + latIndex + lonIndex;
	    return hash;
	}
}
