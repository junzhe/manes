package org.whispercomm.manes.server.http;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.whispercomm.manes.server.domain.DataService;
import org.whispercomm.manes.server.domain.User;

import com.google.inject.Inject;

/**
 * 
 * @author Gulshan Singh
 * 
 */
@Path("/topology/")
public class TopologyResource {

	public final DataService dataService;

	@Inject
	public TopologyResource(DataService dataService) {
		this.dataService = dataService;
	}

	@PUT
	@Path("/recordInRange/{id1:[0-9]+}/{id2:[0-9]+}/")
	public Response recordInRange(@PathParam("id1") User user1,
			@PathParam("id2") User user2) {
		return Response.status(Status.BAD_REQUEST).build();
	}

	@PUT
	@Path("/recordNotInRange/{id1:[0-9]+}/{id2:[0-9]+}/")
	public Response recordNotInRange(@PathParam("id1") User user1,
			@PathParam("id2") User user2) {
		return Response.status(Status.BAD_REQUEST).build();
	}

}
