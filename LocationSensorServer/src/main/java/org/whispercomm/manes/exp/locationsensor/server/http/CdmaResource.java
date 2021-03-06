package org.whispercomm.manes.exp.locationsensor.server.http;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispercomm.manes.exp.locationsensor.data.CDMA;

@Path("/user/{user_id :[0-9]+}/cdma")
public class CdmaResource {

	public static final String CDMA_FILE = "/cdma.json";

	private FileWriter cdmaRecord;
	private ObjectMapper objectMapper;
	private final Logger logger;

	public CdmaResource() {
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updataCdma(@PathParam("user_id") int user_id,
			List<CDMA> cdmas) {
		try {
			// objectMapper.writeValue(cdmaRecord, cdmas);
			Iterator<CDMA> it = cdmas.iterator();
			while (it.hasNext()) {
				cdmaRecord = new FileWriter(UserResource.TRACE_DIR + user_id
						+ CDMA_FILE, true);
				objectMapper.writeValue(cdmaRecord, it.next());
			}
			logger.info("***Successfully wrote cdma data to file!");
			return Response.status(Response.Status.CREATED).build();
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(), e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

}
