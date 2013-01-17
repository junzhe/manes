package org.whispercomm.manes.topology.server.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.server.http.filter.AuthenticatedAs;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.server.manager.TopologyManager;

import com.google.inject.Inject;

@Path("/user/{user_id :[0-9]+}/location")
public class TopologyResource {

	public static final int RESPONSE_STATUS_MORE_DETAIL = 300;

	private final TopologyManager topoManager;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public TopologyResource(TopologyManager topoManager) {
		this.topoManager = topoManager;
	}

	@POST
	@AuthenticatedAs("user_id")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updataLocation(@PathParam("user_id") User user,
			Location location) {
		logger.info("******received a new location update!");
		int user_id = user.getIdentifier();
		// Check the validity of the new location update.
		Location locationLast = null;
		try {
			locationLast = topoManager.preprocess(user_id, location);
		} catch (LocationUpdateBadException e) {
			switch (e.getErrorCode()) {
			case LocationUpdateBadException.UPDATE_INVALID:
				logger.info(e.getMessage(), e);
				return Response.status(Response.Status.BAD_REQUEST).build();
			case LocationUpdateBadException.UPDATE_NO_USER:
				logger.info(e.getMessage(), e);
				return Response.status(Response.Status.BAD_REQUEST).build();
			case LocationUpdateBadException.UPDATE_MORE_DETAIL:
				logger.info(e.getMessage(), e);
				return Response.status(RESPONSE_STATUS_MORE_DETAIL).build();
			default:
				logger.info(e.getMessage(), e);
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		} catch (DataStoreFailureException e) {
			logger.error(e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
		// Update trace Logger.
		topoManager.updateTraceLog(user_id, locationLast, location);
		// Update topology estimation.
		topoManager.updateTopology(user_id, locationLast, location);
		return Response.status(Response.Status.CREATED).build();
	}
}
