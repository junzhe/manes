package org.whispercomm.manes.client.macentity.network;

/**
 * Indicates that the MANES client is not registered with the MANES server.
 * 
 * @author David R. Bild
 * 
 */
public class NotRegisteredException extends Exception {
	private static final long serialVersionUID = 5321085704193163604L;

	public NotRegisteredException() {
		this("The MANES client is not registered with the MANES server.");
	}

	public NotRegisteredException(String msg) {
		super(msg);
	}

}
