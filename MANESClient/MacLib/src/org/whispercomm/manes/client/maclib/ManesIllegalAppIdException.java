package org.whispercomm.manes.client.maclib;

/**
 * Thrown to indicate that the specified application id was invalid.
 * <p>
 * Application ids must be non-negative.
 * 
 * @author Yue Liu
 */

public class ManesIllegalAppIdException extends RuntimeException {

	/**
	 * Generated serial version UUID
	 */
	private static final long serialVersionUID = -2802037580583452932L;

	public ManesIllegalAppIdException() {
		super("app_id has to be no smaller than 0!");
	}

}
