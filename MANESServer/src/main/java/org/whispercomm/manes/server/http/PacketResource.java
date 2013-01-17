package org.whispercomm.manes.server.http;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.http.filter.AuthenticatedAs;

import com.google.inject.Inject;

/**
 * 
 * @author Gulshan Singh
 * @author David R. Bild
 * 
 */
@Path("/user/{user_id :[0-9]+}/packet/")
public class PacketResource {
	public static final String JSON_APP_ID_NAME = "app_id";
	public static final String JSON_CONTENTS_NAME = "packet_contents";

	private final DataService dataService;

	@Inject
	public PacketResource(DataService dataService) {
		this.dataService = dataService;
	}

	static class NewPacketData {
		private Long app_id;
		private byte[] contents;

		public NewPacketData(@JsonProperty(JSON_APP_ID_NAME) Long app_id,
				@JsonProperty(JSON_CONTENTS_NAME) byte[] contents) {
			this.app_id = app_id;
			this.contents = contents;
		}

		@NotNull
		public Long getApp_id() {
			return app_id;
		}

		@NotEmpty
		public byte[] getContents() {
			return contents;
		}
	}

	@POST
	@AuthenticatedAs("user_id")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendNewPacket(@PathParam("user_id") User user,
			NewPacketData data) {
		dataService.createPacket(user, data.getApp_id(), data.getContents(),
				DateTime.now());
		return Response.status(Status.CREATED).build();
	}

}
