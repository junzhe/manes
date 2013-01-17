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
import org.whispercomm.manes.exp.locationsensor.data.Gyro;

@Path("/user/{user_id :[0-9]+}/gyro/")
public class GyroResource {

	public static final String GYRO_FILE = "/gyro.json";

	private FileWriter gyroRecord;
	private ObjectMapper objectMapper;
	private final Logger logger;

	public GyroResource() {
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updataGyro(@PathParam("user_id") int user_id,
			List<Gyro> gyros) {
		try {
			// objectMapper.writeValue(gyroRecord, gyros);
			Iterator<Gyro> it = gyros.iterator();
			while (it.hasNext()) {
				gyroRecord = new FileWriter(UserResource.TRACE_DIR + user_id
						+ GYRO_FILE, true);
				objectMapper.writeValue(gyroRecord, it.next());
			}
			logger.info("***Successfully wrote gyro data to file!");
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
