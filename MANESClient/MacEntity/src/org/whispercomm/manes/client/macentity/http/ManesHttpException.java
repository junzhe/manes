package org.whispercomm.manes.client.macentity.http;

/**
 * General exception class used to signify failures relating to sending HTTP
 * requests to the MANES server.
 * 
 * @author David Adrian
 * 
 */
public class ManesHttpException extends Exception {

	/**
	 * Generated serialVersionUID for serialization
	 */
	private static final long serialVersionUID = -1494462857289891509L;

	public ManesHttpException() {
	}

	public ManesHttpException(String detailMessage) {
		super(detailMessage);
	}

	public ManesHttpException(Throwable throwable) {
		super(throwable);
	}

	public ManesHttpException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
