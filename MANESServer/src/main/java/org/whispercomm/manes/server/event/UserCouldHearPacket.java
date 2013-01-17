package org.whispercomm.manes.server.event;

import org.whispercomm.manes.server.domain.User;

/**
 * {@link DomainEvent} raised when a new COULD_HEAR relationship between a
 * {@link User} and a new {@link Packet}.
 * 
 * @author David R. Bild
 * 
 */
public class UserCouldHearPacket implements DomainEvent {
	private final User user;

	public UserCouldHearPacket(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
