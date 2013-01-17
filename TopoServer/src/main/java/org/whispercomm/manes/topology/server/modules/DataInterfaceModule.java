package org.whispercomm.manes.topology.server.modules;

import org.whispercomm.manes.topology.server.data.DataInterface;
import org.whispercomm.manes.topology.server.data.DataInterfaceVoldemort;

import com.google.inject.AbstractModule;

public class DataInterfaceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DataInterface.class).to(DataInterfaceVoldemort.class);
	}

}
