package org.whispercomm.manes.topology.server.filtertest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class OauthfilterTest implements ContainerRequestFilter {

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		System.out.println(1);
		return null;
	}

}
