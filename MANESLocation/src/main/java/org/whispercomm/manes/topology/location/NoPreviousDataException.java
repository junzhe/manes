package org.whispercomm.manes.topology.location;

public class NoPreviousDataException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -389310214266426895L;
	public static final String MESSAGE = "No corresponding information in previous location!";

	public NoPreviousDataException(String message) {
		super(message);
	}

	public NoPreviousDataException() {
		super(MESSAGE);
	}

}
