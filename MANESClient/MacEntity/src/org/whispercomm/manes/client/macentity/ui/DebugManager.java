package org.whispercomm.manes.client.macentity.ui;

import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.http.ManesHttpException;
import org.whispercomm.manes.client.macentity.network.IdManager;
import org.whispercomm.manes.client.macentity.network.ManesService;
import org.whispercomm.manes.client.macentity.network.NotRegisteredException;

import android.util.Log;

public class DebugManager {

	private static final String TAG = DebugManager.class.getSimpleName();

	private HttpManager httpManager;
	private IdManager idManager;

	public DebugManager(IdManager idManager, HttpManager httpManager) {
		this.httpManager = httpManager;
		this.idManager = idManager;
	}

	public boolean registerDevice() {
		if (idManager.isRegistered()) {
			return true;
		} else {
			try {
				idManager.register(httpManager);
				if (idManager.isRegistered()) {
					return true;
				}
			} catch (ManesHttpException e) {
				Log.e(TAG, e.getMessage());
			}
			return false;
		}
	}

	public String getUserIdDisplay() {
		try {
			return String.format("%d", idManager.getUserId());
		} catch (NotRegisteredException e) {
			return "Not registered; no server-assigned ID";
		}
	}

	public String getServerAddress() {
		return ManesService.SERVER_ADDRESS;
	}

	public String getServerBaseUrl() {
		return ManesService.SERVER_URL;
	}

	public boolean getRegistrationStatus() {
		return idManager.isRegistered();
	}
}
