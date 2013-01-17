package org.whispercomm.manes.exp.locationsensor.server.http;

/**
 * Exception throw for invalid or incomplete location updates.
 * 
 * @author Yue Liu
 * 
 */
public class LocationUpdateBadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3506085548952965220L;
	/**
	 * Invalid location data.
	 */
	public static final int UPDATE_INVALID = -1;
	/**
	 * User does not exist.
	 */
	public static final int UPDATE_NO_USER = -2;
	/**
	 * Detailed location information needed from the client. For example, the
	 * "user_id"'s historical information is lost and newly updated "location"
	 * says "the data is as-previous".
	 */
	public static final int UPDATE_MORE_DETAIL = -3;

	private int errorCode;

	public LocationUpdateBadException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
