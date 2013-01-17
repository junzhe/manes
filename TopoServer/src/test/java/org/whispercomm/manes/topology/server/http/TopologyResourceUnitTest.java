package org.whispercomm.manes.topology.server.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.server.domain.User;
import org.whispercomm.manes.topology.location.testutility.Utility;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner;
import org.whispercomm.manes.topology.server.GuiceJUnitRunner.GuiceModules;
import org.whispercomm.manes.topology.server.manager.TopologyManager;
import org.whispercomm.manes.topology.server.manager.TopologyManager4Test;
import org.whispercomm.manes.topology.server.modules.TestModule;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@RunWith(GuiceJUnitRunner.class)
@GuiceModules({ TestModule.class })
public class TopologyResourceUnitTest {
	TopologyResource resource;
	TopologyManager4Test topoManager;

	@Inject
	public void setUp(@Named("Data") TopologyManager topoManager) {
		this.topoManager = (TopologyManager4Test) topoManager;
		resource = new TopologyResource(this.topoManager);
	}

	@Test
	public void updataLocationTest() {
		User user = mock(User.class);
		when(user.getIdentifier()).thenReturn(0);
		resource.updataLocation(user, Utility.initLocationInstance());
		assertTrue(topoManager.preprocessCalled);
		assertTrue(topoManager.updateTopologyCalled);
		assertTrue(topoManager.updateTraceLogCalled);
	}
}
