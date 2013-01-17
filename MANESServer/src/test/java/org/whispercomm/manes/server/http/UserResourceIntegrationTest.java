package org.whispercomm.manes.server.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.whispercomm.manes.test.Matchers.equivalentJsonTo;
import static org.whispercomm.manes.test.Matchers.equivalentUnorderedJsonArrayTo;

import javax.ws.rs.core.MediaType;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.DoesNotExistException;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.domain.UserService;
import org.whispercomm.manes.server.http.provider.OAuthProviderImpl;
import org.whispercomm.manes.server.http.provider.UserPathParam;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;

public class UserResourceIntegrationTest extends JerseyTest {
	private static final String JSON_FORMAT_REGISTER = "{ \"secret\" : \"%s\" }";

	public static final String PROVIDERS_PACKAGE_NAME = "org.whispercomm.manes.server.http.provider";

	/**
	 * Mock data service.
	 */
	private static final DataService dataService = mock(DataService.class);
	private static final UserService userService = mock(UserService.class);

	/**
	 * Mock User.
	 */
	private User newUser;
	private User existUser;

	private static AppDescriptor buildAppDescriptor() {
		LowLevelAppDescriptor app = new LowLevelAppDescriptor.Builder(
				PROVIDERS_PACKAGE_NAME).contextPath("/").build();
		app.getResourceConfig().getSingletons()
				.add(new UserResource(dataService));
		// These removals should happen automatically, but it doesn't.
		app.getResourceConfig().getClasses().remove(UserPathParam.class);
		app.getResourceConfig().getClasses().remove(OAuthProviderImpl.class);
		app.getResourceConfig().getSingletons()
				.add(new UserPathParam(userService));
		app.getResourceConfig().getSingletons()
				.add(new OAuthProviderImpl(userService));
		return app;
	}

	public UserResourceIntegrationTest() {
		super(buildAppDescriptor());
	}

	@Before
	public void setup() {
		newUser = mock(User.class);
		existUser = mock(User.class);

		when(dataService.createUser(validSecret)).thenReturn(newUser);
		when(newUser.getIdentifier()).thenReturn(1);

		try {
			when(dataService.getUser(2)).thenReturn(existUser);
		} catch (DoesNotExistException e) {
		}
		try {
			when(userService.getUser(2)).thenReturn(existUser);
		} catch (DoesNotExistException e) {
		}
		when(existUser.getSharedSecret()).thenReturn(validSecret);
	}

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new InMemoryTestContainerFactory();
	}

	private static final String validSecret = "01234567890123456789012345678901";
	private static final String validNewSecret = "new01234567890123456789012345678901";
	private static final String tooShortSecret = "0123456789012345678901234567890";
	private static final String tooLongSecret = "012345678901234567890123456789"
			+ "012345678901234567890123456789"
			+ "012345678901234567890123456789" + "0123456789" + "0";
	private static final String emptySecret = "";

	private static final String validC2dmReg = "myc2dmregistration";

	/**
	 * Sends the given json to the instance under test and verifies the result.
	 * 
	 * @param requestJson
	 *            json request to send
	 * @param expectedResponseCode
	 *            status code that should be returned
	 * @param expectedResponseJson
	 *            json that should be returned
	 */
	private void testNewUser(String requestJson, int expectedResponseCode,
			Matcher<String> expectedMatcher) {
		ClientResponse response = resource().path("user/")
				.accept(MediaType.APPLICATION_JSON)
				.entity(requestJson, MediaType.APPLICATION_JSON)
				.post(ClientResponse.class);

		assertThat("response status code", response.getStatus(),
				is(expectedResponseCode));
		assertThat("response mime type", response.getType(),
				is(MediaType.APPLICATION_JSON_TYPE));
		assertThat("response json", response.getEntity(String.class),
				is(expectedMatcher));
	}

	private void testNewUser(String requestJson, int expectedResponseCode,
			String expectedResponseJson) {
		testNewUser(requestJson, expectedResponseCode,
				equivalentJsonTo(expectedResponseJson));
	}

	private void testUpdateUser(String requestJson, int expectedResponseCode,
			Matcher<String> expectedMatcher) {
		ClientResponse response = resource().path("user/2/")
				.accept(MediaType.APPLICATION_JSON)
				.entity(requestJson, MediaType.APPLICATION_JSON)
				.put(ClientResponse.class);

		assertThat("response status code", response.getStatus(),
				is(expectedResponseCode));
		if (expectedMatcher != null) {
			assertThat("response mime type", response.getType(),
					is(MediaType.APPLICATION_JSON_TYPE));
			assertThat("response json", response.getEntity(String.class),
					is(expectedMatcher));
		}
	}

	private void testUpdateUser(String requestJson, int expectedResponseCode,
			String expectedResponseJson) {
		testUpdateUser(requestJson, expectedResponseCode,
				equivalentJsonTo(expectedResponseJson));
	}

	private void testUpdateUser(String requestJson, int expectedResponseCode) {
		testUpdateUser(requestJson, expectedResponseCode,
				(Matcher<String>) null);
	}

	@Test
	public void validNewUser() {
		String requestJson = String.format(JSON_FORMAT_REGISTER, validSecret);
		String responseJson = "{\"user_id\" : 1}";
		testNewUser(requestJson, 201, responseJson);
	}

	@Test
	public void validSecretUpdateUser() {
		String requestJson = String.format("{ \"secret\" : \"%s\" }",
				validNewSecret);
		testUpdateUser(requestJson, 200);
		verify(existUser).setSharedSecret(validNewSecret);
	}

	@Test
	public void validAllUpdateUser() {
		String requestJson = String
				.format(JSON_FORMAT_REGISTER, validNewSecret);
		testUpdateUser(requestJson, 200);
		verify(existUser).setSharedSecret(validNewSecret);
	}

	@Test
	public void tooShortSecretNewUser() {
		String requestJson = String.format(JSON_FORMAT_REGISTER,
				tooShortSecret, validC2dmReg);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						tooShortSecret);
		testNewUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void tooShortSecretUpdateUser() {
		String requestJson = String.format("{ \"secret\" : \"%s\" }",
				tooShortSecret);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						tooShortSecret);
		testUpdateUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void tooLongSecretNewUser() {
		String requestJson = String.format(JSON_FORMAT_REGISTER, tooLongSecret,
				validC2dmReg);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						tooLongSecret);
		testNewUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void tooLongSecretUpdateUser() {
		String requestJson = String.format("{ \"secret\" : \"%s\" }",
				tooLongSecret);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						tooLongSecret);
		testUpdateUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void emptySecretNewUser() {
		String requestJson = String.format(JSON_FORMAT_REGISTER, emptySecret,
				validC2dmReg);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						emptySecret);
		testNewUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void emptySecretUpdateUser() {
		String requestJson = String.format("{ \"secret\" : \"%s\" }",
				emptySecret);
		String responseJson = String
				.format("[{\"key\":\"secret\", \"value\":\"%s\", \"error\":\"size must be between 32 and 100\"}]",
						emptySecret);
		testUpdateUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void missingSecretNewUser() {
		String requestJson = "{ }";
		String responseJson = "[{\"key\":\"secret\", \"value\":null, \"error\":\"may not be null\"}]";
		testNewUser(requestJson, 400,
				equivalentUnorderedJsonArrayTo(responseJson));
	}

	@Test
	public void extraJsonKeyNewUser() {
		String requestJson = String
				.format("{ \"secret\" : \"%s\", \"extra\" : \"parameter\" }",
						validSecret);
		String responseJson = "{ \"error\" : \"Unexpected field: extra at Line: 1, Column: 61\" }";
		testNewUser(requestJson, 400, responseJson);
	}

	@Test
	public void extraJsonKeyUpdateUser() {
		String requestJson = String
				.format("{ \"secret\" : \"%s\", \"extra\" : \"parameter\" }",
						validSecret);
		String responseJson = "{ \"error\" : \"Unexpected field: extra at Line: 1, Column: 61\" }";
		testUpdateUser(requestJson, 400, responseJson);
	}

	@Test
	public void incorrectJsonObjectNewUser() {
		String requestJson = String.format(
				"[{ secret\" : \"%s\", \"c2dm_reg_id\" : \"%s\" }]",
				validSecret, validC2dmReg);
		String responseJson = "{ \"error\" : \"Unexpected JSON array or object at Line: 1, Column: 1\"}";
		testNewUser(requestJson, 400, responseJson);
	}

	@Test
	public void incorrectJsonObjectUpdateUser() {
		String requestJson = String.format(
				"[{ secret\" : \"%s\", \"c2dm_reg_id\" : \"%s\" }]",
				validSecret, validC2dmReg);
		String responseJson = "{ \"error\" : \"Unexpected JSON array or object at Line: 1, Column: 1\"}";
		testUpdateUser(requestJson, 400, responseJson);
	}

	@Test
	public void invalidJsonNewUser() {
		String requestJson = String.format(
				"{ secret\" : \"%s\", \"c2dm_reg_id\" : \"%s\" }", validSecret,
				validC2dmReg); // Missing left quote for key 'secret'
		String responseJson = "{ \"error\" : \"Invalid JSON syntax at Line: 1, Column: 4\"}";
		testNewUser(requestJson, 400, responseJson);
	}

	@Test
	public void invalidJsonUpdateUser() {
		String requestJson = String.format(
				"{ secret\" : \"%s\", \"c2dm_reg_id\" : \"%s\" }", validSecret,
				validC2dmReg); // Missing left quote for key 'secret'
		String responseJson = "{ \"error\" : \"Invalid JSON syntax at Line: 1, Column: 4\"}";
		testUpdateUser(requestJson, 400, responseJson);
	}

}
