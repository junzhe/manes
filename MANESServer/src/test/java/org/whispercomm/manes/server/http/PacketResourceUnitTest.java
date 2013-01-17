package org.whispercomm.manes.server.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.Packet;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.http.PacketResource.NewPacketData;

/**
 * 
 * @author Gulshan Singh
 * @author David R. Bild
 * 
 */
public class PacketResourceUnitTest {

	private static final long app_id = 1;

	private static final byte[] contents = new byte[] { 0, 45, 95, 100, 127,
			-128, -103, -74, -34, -1 };

	private static final Validator validator = Validation
			.buildDefaultValidatorFactory().getValidator();

	private DataService dataService;

	private User user;


	private Packet packetA;


	private PacketResource cut;

	@Before
	public void setup() {
		dataService = mock(DataService.class);
		user = mock(User.class);
		packetA = mock(Packet.class);
		cut = new PacketResource(dataService);
	}

	/* --------------- Test SendPacketData Verification --------------- */

	@Test
	public void packetDataGoodValuesAreValid() {
		NewPacketData data = new NewPacketData(app_id, contents);
		Set<ConstraintViolation<NewPacketData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(Matchers.<ConstraintViolation<NewPacketData>> empty()));
	}

	@Test
	public void packetDataNullAppIdIsInvalid() {
		NewPacketData data = new NewPacketData(null, contents);
		Set<ConstraintViolation<NewPacketData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<NewPacketData>> empty())));
	}

	@Test
	public void packetDataNullPacketContentsIsInvalid() {
		NewPacketData data = new NewPacketData(app_id, null);
		Set<ConstraintViolation<NewPacketData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<NewPacketData>> empty())));
	}

	@Test
	public void packetDataEmptyPacketContentsIsInvalid() {
		NewPacketData data = new NewPacketData(app_id, new byte[] {});
		Set<ConstraintViolation<NewPacketData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<NewPacketData>> empty())));
	}

	/* --------------------- Test Resource Methods -------------------- */

	@Test
	public void sendPacketTest() {
		NewPacketData data = new NewPacketData(app_id, contents);
		when(
				dataService.createPacket(eq(user), eq(app_id), eq(contents),
						(DateTime) any(DateTime.class))).thenReturn(packetA);

		Response response = cut.sendNewPacket(user, data);

		verify(dataService).createPacket(eq(user), eq(app_id), eq(contents),
				(DateTime) any(DateTime.class));
		assertThat(response.getStatus(), is(201));
	}

}
