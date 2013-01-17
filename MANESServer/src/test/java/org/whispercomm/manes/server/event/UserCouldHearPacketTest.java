package org.whispercomm.manes.server.event;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.server.domain.User;

/**
 * Unit tests for {@link UserCouldHearPacket}.
 * 
 * @author David R. Bild
 * 
 */
public class UserCouldHearPacketTest {

	private User user;

	private UserCouldHearPacket cut;

	@Before
	public void setup() {
		user = mock(User.class);
		cut = new UserCouldHearPacket(user);
	}

	@Test
	public void getUserReturnsConstructorParameter() {
		assertThat(cut.getUser(), is(user));
	}

}
