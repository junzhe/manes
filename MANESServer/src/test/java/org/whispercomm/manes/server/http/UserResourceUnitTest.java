package org.whispercomm.manes.server.http;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.whispercomm.manes.test.Matchers.equivalentJsonTo;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.DoesNotExistException;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.http.UserResource.UserRegistrationData;
import org.whispercomm.manes.server.http.UserResource.UserUpdateData;

public class UserResourceUnitTest {

	private static final String secret = "01234567890123456789012345678901";

	private static final Validator validator = Validation
			.buildDefaultValidatorFactory().getValidator();

	private DataService dataService;
	private User user;

	private UserResource cut;

	@Before
	public void setup() {
		dataService = mock(DataService.class);
		user = mock(User.class);
		cut = new UserResource(dataService);
	}

	/* ------------ Test UserRegistrationData Verification ------------ */
	@Test
	public void userRegistrationDataGoodValuesAreValid() {
		UserRegistrationData data = new UserRegistrationData(secret);
		Set<ConstraintViolation<UserRegistrationData>> violations = validator
				.validate(data);
		assertThat(
				violations,
				is(Matchers.<ConstraintViolation<UserRegistrationData>> empty()));
	}

	@Test
	public void userRegistrationDataNullSecretIsInvalid() {
		UserRegistrationData data = new UserRegistrationData(null);
		Set<ConstraintViolation<UserRegistrationData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers
						.<ConstraintViolation<UserRegistrationData>> empty())));
	}

	@Test
	public void userRegistrationDataShortSecretIsInvalid() {
		UserRegistrationData data = new UserRegistrationData(
				"0123456789012345678901234567890");
		Set<ConstraintViolation<UserRegistrationData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers
						.<ConstraintViolation<UserRegistrationData>> empty())));
	}

	@Test
	public void userRegistrationDataLongSecretIsInvalid() {
		UserRegistrationData data = new UserRegistrationData(
				"012345678901234567890123456789012345678901234567890123456789"
						+ "01234567890123456789012345678901234567890");
		Set<ConstraintViolation<UserRegistrationData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers
						.<ConstraintViolation<UserRegistrationData>> empty())));
	}

	/* --------------- Test UserUpdateData Verification --------------- */
	@Test
	public void userUpdateDataGoodValuesAreValid() {
		UserUpdateData data = new UserUpdateData(secret);
		Set<ConstraintViolation<UserUpdateData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(Matchers.<ConstraintViolation<UserUpdateData>> empty()));
	}

	@Test
	public void userUpdateDataNullSecretIsNotValid() {
		UserUpdateData data = new UserUpdateData(null);
		Set<ConstraintViolation<UserUpdateData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<UserUpdateData>> empty())));
	}

	@Test
	public void userUpdateDataShortSecretIsInvalid() {
		UserUpdateData data = new UserUpdateData(
				"0123456789012345678901234567890");
		Set<ConstraintViolation<UserUpdateData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<UserUpdateData>> empty())));
	}

	@Test
	public void userUpdateDataLongSecretIsInvalid() {
		UserUpdateData data = new UserUpdateData(
				"012345678901234567890123456789012345678901234567890123456789"
						+ "01234567890123456789012345678901234567890");
		Set<ConstraintViolation<UserUpdateData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<UserUpdateData>> empty())));
	}

	@Test
	public void userUpdateDataEmptyC2DMIsInvalid() {
		UserUpdateData data = new UserUpdateData(null);
		Set<ConstraintViolation<UserUpdateData>> violations = validator
				.validate(data);
		assertThat(violations,
				is(not(Matchers.<ConstraintViolation<UserUpdateData>> empty())));
	}

	/* --------------------- Test Resource Methods -------------------- */
	@Test
	public void registerNewUserCreatesNewUser() {
		UserRegistrationData data = new UserRegistrationData(secret);
		when(dataService.createUser(secret)).thenReturn(user);
		when(user.getIdentifier()).thenReturn(42);

		Response response = cut.registerNewUser(data);

		verify(dataService).createUser(secret);
		assertThat(response.getStatus(), is(201));
		assertThat((String) response.getEntity(),
				is(equivalentJsonTo(String.format("{\"user_id\" : %d}", 42L))));
	}

	private void configureUserUpdateTest() throws DoesNotExistException {
		when(dataService.getUser(42)).thenReturn(user);
	}

	private void verifySuccessfulUserUpdateTest(Response response) {
		assertThat(response.getStatus(), is(200));
	}

	@Test
	public void updateUserUpdatesBothGivenParameters()
			throws DoesNotExistException {
		configureUserUpdateTest();
		UserUpdateData data = new UserUpdateData(secret);

		Response response = cut.updateUser(user, data);

		verify(user).setSharedSecret(secret);
		verifySuccessfulUserUpdateTest(response);
	}

	@Test
	public void updateUserUpdatesOnlyGivenParameterSecret()
			throws DoesNotExistException {
		configureUserUpdateTest();
		UserUpdateData data = new UserUpdateData(secret);

		Response response = cut.updateUser(user, data);

		verify(user).setSharedSecret(secret);
		verifySuccessfulUserUpdateTest(response);
	}
}
