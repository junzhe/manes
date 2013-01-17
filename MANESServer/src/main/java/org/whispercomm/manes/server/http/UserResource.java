package org.whispercomm.manes.server.http;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.http.filter.AuthenticatedAs;

import com.google.inject.Inject;

@Path("/user/")
public class UserResource {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UserResource.class);

	public static final String JSON_SHARED_SECRET_NAME = "secret";
	public static final String JSON_C2DM_REG_ID_NAME = "c2dm_reg_id";

	private final DataService dataService;

	@Inject
	public UserResource(DataService dataService) {
		this.dataService = dataService;
	}

	static class UserRegistrationData {
		private String secret;

		public UserRegistrationData(
				@JsonProperty(JSON_SHARED_SECRET_NAME) String secret) {
			this.secret = secret;
		}

		@NotNull
		@Size(min = 32, max = 100)
		public String getSecret() {
			return secret;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerNewUser(UserRegistrationData userData) {
		User newUser = this.dataService.createUser(userData.getSecret());
		return Response
				.status(Status.CREATED)
				.entity(String.format("{\"user_id\" : %d}",
						newUser.getIdentifier())).build();
	}

	static class UserUpdateData {
		private String secret;

		public UserUpdateData(
				@JsonProperty(JSON_SHARED_SECRET_NAME) String secret) {
			this.secret = secret;
		}

		@NotNull
		@Size(min = 32, max = 100)
		public String getSecret() {
			return secret;
		}
	}

	@PUT
	@Path("{user_id:[0-9]+}/")
	@AuthenticatedAs("user_id")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("user_id") User user,
			UserUpdateData userData) {
			user.setSharedSecret(userData.getSecret());
			user.writeBack();
			LOGGER.info("Updated shared secret for user with id {}",
					user.getIdentifier());
			return Response.ok().build();
	}
}
