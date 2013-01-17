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
import org.whispercomm.manes.exp.locationsensor.data.GSM;

@Path("/user/{user_id :[0-9]+}/gsm")
public class GsmResource {

	public static final String GSM_FILE = "/gsm.json";

	private FileWriter gsmRecord;
	private ObjectMapper objectMapper;
	private final Logger logger;

	public GsmResource() {
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updataGsm(@PathParam("user_id") int user_id, List<GSM> gsms) {
		try {
			// objectMapper.writeValue(gsmRecord, gsms);
			Iterator<GSM> it = gsms.iterator();
			while (it.hasNext()) {
				gsmRecord = new FileWriter(UserResource.TRACE_DIR + user_id
						+ GSM_FILE, true);
				objectMapper.writeValue(gsmRecord, it.next());
			}
			logger.info("***Successfully wrote gsm data to file!");
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
