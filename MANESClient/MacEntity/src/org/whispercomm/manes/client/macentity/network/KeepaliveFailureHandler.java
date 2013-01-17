package org.whispercomm.manes.client.macentity.network;

/**
 * This interface handles failures of sending keepalive packets.
 * 
 * @author Yue Liu
 * 
 */
public interface KeepaliveFailureHandler {

	public void handle(Throwable e);
}
