package org.whispercomm.manes.exp.locationsensor.server.http;

import java.io.File;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.inject.Inject;

@Path("/user/")
public class UserResource {
	public static final String TRACE_DIR = "/home/ec2-user/locsensor-traces/";

	public static final String JSON_SHARED_SECRET_NAME = "secret";
	public static final String JSON_C2DM_REG_ID_NAME = "c2dm_reg_id";

	private final RecordingIdGenerator idGenerator;

	@Inject
	public UserResource(RecordingIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
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
		// generate user id and create trace directory for this user.
		int id = idGenerator.getNextIdentifier();
		new File(TRACE_DIR + id).mkdir();
		return Response.status(Status.CREATED)
				.entity(String.format("{\"user_id\" : %d}", id)).build();
	}
}
