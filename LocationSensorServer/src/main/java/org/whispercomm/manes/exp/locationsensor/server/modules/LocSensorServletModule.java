package org.whispercomm.manes.exp.locationsensor.server.modules;

import org.whispercomm.manes.exp.locationsensor.server.http.AccelerometerResource;
import org.whispercomm.manes.exp.locationsensor.server.http.CdmaResource;
import org.whispercomm.manes.exp.locationsensor.server.http.GpsResource;
import org.whispercomm.manes.exp.locationsensor.server.http.GsmResource;
import org.whispercomm.manes.exp.locationsensor.server.http.GyroResource;
import org.whispercomm.manes.exp.locationsensor.server.http.LightResource;
import org.whispercomm.manes.exp.locationsensor.server.http.MagnetResource;
import org.whispercomm.manes.exp.locationsensor.server.http.RecordingIdGenerator;
import org.whispercomm.manes.exp.locationsensor.server.http.UserResource;
import org.whispercomm.manes.exp.locationsensor.server.http.WifiDirectResource;
import org.whispercomm.manes.exp.locationsensor.server.http.WifiResource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Configures the location sensor server.
 * 
 * @author Yue Liu
 * 
 */
public class LocSensorServletModule extends JerseyServletModule {

	protected void configureInitParams(
			ImmutableMap.Builder<String, String> initParams) {
		initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
	}

	@Override
	protected void configureServlets() {
		bind(GuiceContainer.class);
		bind(AccelerometerResource.class).in(Scopes.SINGLETON);
		bind(CdmaResource.class).in(Scopes.SINGLETON);
		bind(GpsResource.class).in(Scopes.SINGLETON);
		bind(GsmResource.class).in(Scopes.SINGLETON);
		bind(GyroResource.class).in(Scopes.SINGLETON);
		bind(LightResource.class).in(Scopes.SINGLETON);
		bind(MagnetResource.class).in(Scopes.SINGLETON);
		bind(WifiResource.class).in(Scopes.SINGLETON);
		bind(WifiDirectResource.class).in(Scopes.SINGLETON);
		bind(UserResource.class).in(Scopes.SINGLETON);
		bind(RecordingIdGenerator.class).in(Scopes.SINGLETON);
		ImmutableMap.Builder<String, String> initParams = ImmutableMap
				.<String, String> builder();
		configureInitParams(initParams);
		serve("*").with(GuiceContainer.class, initParams.build());
	}

}
