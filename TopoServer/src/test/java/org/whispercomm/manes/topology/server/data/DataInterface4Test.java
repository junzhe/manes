package org.whispercomm.manes.topology.server.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.whispercomm.manes.server.expirablestore.DataStoreFailureException;
import org.whispercomm.manes.topology.location.*;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.http.LocationUpdateBadException;


public class DataInterface4Test implements DataInterface {

	@Override
	public Location getClientLocation(int user_id)
			throws DataStoreFailureException {
		Location loc = new Location();
		GPS gps = new GPS();
		loc.setGps(gps);
		switch(user_id){
		case 0:
			return Utility.initLocationInstance();
		case 1:
			gps.setLat(55.56);
			gps.setLon(66.66);
			break;
		case 2:
			gps.setLat(56.00);
			gps.setLon(67.00);
			break;
		case 3:
			gps.setLat(90.00);
			gps.setLon(00.00);
			break;
		}
		return loc;
	}

	@Override
	public void updateClientLocation(int user_id, Location location)
			throws DataStoreFailureException {
	}

	@Override
	public Location interpretAndThenUpdateClientLocation(int user_id,
			Location locationRaw) throws LocationUpdateBadException,
			DataStoreFailureException {
		return null;
	}

	@Override
	public Set<Integer> getAPClient(long AP_Mac)
			throws DataStoreFailureException {
		return null;
	}

	@Override
	public Set<Integer> getGPSGridClient(GpsGridElement grid)
			throws DataStoreFailureException {
		Set<Integer> result = new HashSet<Integer>();
		result.add(0);
		result.add(1);
		result.add(2);
		result.add(3);
		return result;
	}

	@Override
	public Map<Integer, Float> getClientNeighbors(int user_id)
			throws DataStoreFailureException {
		return null;
	}

	@Override
	public void setClientNeighbors(int user_id, Map<Integer, Float> neighbors)
			throws DataStoreFailureException {
	}

}
