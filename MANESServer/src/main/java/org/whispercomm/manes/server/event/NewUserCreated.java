package org.whispercomm.manes.server.event;

import org.whispercomm.manes.server.domain.User;

/**
 * {@link DomainEvent} raised when a new {@link User} is created.
 * 
 * @author David R. Bild
 * 
 */
public class NewUserCreated implements DomainEvent {
	private final User user;

	public NewUserCreated(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
}
