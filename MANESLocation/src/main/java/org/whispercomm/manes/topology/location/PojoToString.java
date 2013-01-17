package org.whispercomm.manes.topology.location;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

/**
 * Utility class for translating POJO(->JSON)->String.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 *
 */
public class PojoToString {
	
	/**
	 * Translate a POJO object to a JSON string. 
	 * 
	 * @param t the POJO object
	 * @return
	 */
	public static <T> String toString(T t) {
		StringBuffer sb = new StringBuffer();
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(t.getClass());
		JSONObject json = (JSONObject) JSONSerializer.toJSON(t);
		sb.append(json.toString());
		return sb.toString();
	}
}
