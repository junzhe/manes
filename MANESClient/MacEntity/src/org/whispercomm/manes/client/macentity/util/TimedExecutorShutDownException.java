package org.whispercomm.manes.client.macentity.util;

/**
 * This exception is thrown when one tries to use a {@link TimedExecutor} that
 * has already been shut down.
 * 
 * @author Yue Liu
 * 
 */
public class TimedExecutorShutDownException extends RuntimeException {

	private static final long serialVersionUID = -1180845019461308939L;

	public TimedExecutorShutDownException() {
		super(
				"The TimedExecutor instance is already shut down. No longer handle any further schedule requests.");
	}

}
