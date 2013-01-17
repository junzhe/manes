package org.whispercomm.manes.exp.locationsensor.location;

/**
 * This exception is thrown by {@link TopologyServerSynchronizer} when it is
 * asked for the latest server record, in the situation that the client has lost
 * synchronization with the server.
 * 
 * @author Yue Liu
 * 
 */
public class ServerUnSyncedException extends Exception {

	private static final long serialVersionUID = 6809718891022473449L;

	public ServerUnSyncedException() {
		super("The server is currently out of sync with the client.");
	}
}
