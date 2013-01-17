package org.whispercomm.manes.client.macentity.network;

/**
 * This {@link Exception} notifies the failure of sending a keepalive packet to
 * the MANES server.
 * 
 * @author Yue Liu
 * 
 */
public class KeepaliveFailureException extends Exception {

	private static final long serialVersionUID = -830174363204040063L;

	public KeepaliveFailureException() {
		super("Failed to send keepalive to MANES server!");
	}

	public KeepaliveFailureException(Throwable e) {
		super(e);
	}
}
