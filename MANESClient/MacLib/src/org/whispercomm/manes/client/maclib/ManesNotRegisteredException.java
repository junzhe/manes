package org.whispercomm.manes.client.maclib;

/**
 * Indicates that the MANES client is not registered with the MANES server.
 * 
 * @author David R. Bild
 * 
 */
public class ManesNotRegisteredException extends Exception {
	private static final long serialVersionUID = 471104385526889967L;

	public ManesNotRegisteredException() {
		this("The MANES client is not registered with the MANES server.");
	}

	public ManesNotRegisteredException(String msg) {
		super(msg);
	}

}
