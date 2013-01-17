package org.whispercomm.manes.client.maclib;


/**
 * Thrown to indicate that the supplied packet was larger than
 * {@link ManesInterface#MANES_MTU MANES_MTU}.
 * 
 * @author Yue Liu
 * 
 */
public class ManesFrameTooLargeException extends RuntimeException {
	
	/**
	 * Generated serial-version UUID
	 */
	private static final long serialVersionUID = -1722804620433762134L;

	public ManesFrameTooLargeException() {
		super();
	}

	public ManesFrameTooLargeException(String detailMessage) {
		super(detailMessage);
	}




}
