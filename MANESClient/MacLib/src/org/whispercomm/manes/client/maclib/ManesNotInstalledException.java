package org.whispercomm.manes.client.maclib;

/**
 * Indicates that the MANES client application is not installed.
 * 
 * @author David R. Bild
 * 
 */
public class ManesNotInstalledException extends Exception {
	private static final long serialVersionUID = 5544687454380892368L;

	public ManesNotInstalledException() {
		super();
	}

	public ManesNotInstalledException(String msg) {
		super(msg);
	}
}
