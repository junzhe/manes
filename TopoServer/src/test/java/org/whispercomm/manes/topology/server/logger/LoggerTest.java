package org.whispercomm.manes.topology.server.logger;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import junit.framework.Assert;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import org.junit.Test;
import org.whispercomm.manes.topology.location.Location;
import org.whispercomm.manes.topology.location.testutility.Utility;

public class LoggerTest {

	@Test
	public void testLocationLog() {
		UserLogManager user = new UserLogManager(1, "locInfo", "/location.log",
				"%d\t%m\n", false);
		Location location = Utility.initLocationInstance();
		user.info(location);
		try {
			FileInputStream fstream = new FileInputStream("user-traces/" + 1
					+ "/location.log");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			while(br.ready()){
				strLine = br.readLine();
			}
			int start = strLine.indexOf('\t');
			String sub = strLine.substring(start + 1);
			sub = sub.trim();
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setRootClass(Location.class);
			JSONObject json = (JSONObject) JSONSerializer.toJSON(location);
			Assert.assertEquals(sub, json.toString());
			br.close();
		} catch (FileNotFoundException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testTopoLog() {
		UserLogManager user = new UserLogManager(1, "topoInfo", "/topo.log",
				"%d\t%m\n", false);
		JSONObject json = new JSONObject();
		HashMap<Integer, Float> links = new HashMap<Integer, Float>();
		links.put(1, 11.1f);
		links.put(2, 22.2f);
		links.put(3, 33.3f);
		json.put("topo", links.toString());
		user.setAdditivity(false);
		user.info(json.toString());
		try {
			FileInputStream fstream = new FileInputStream("user-traces/" + 1
					+ "/topo.log");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			while(br.ready()){
				strLine = br.readLine();
			}
			int start = strLine.indexOf('\t');
			String sub = strLine.substring(start + 1);
			sub = sub.trim();
			Assert.assertEquals(sub, json.toString());
			br.close();
		} catch (FileNotFoundException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
}
