package org.whispercomm.manes.client.macentity.http;

import java.io.IOException;
import java.io.StringBufferInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.location.LocationSender;
import org.whispercomm.manes.client.macentity.location.TopologyServerSynchronizer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Matchers.anyInt;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@SuppressWarnings("deprecation")
@RunWith(RobolectricTestRunner.class)
public class LocationResponseHandlerTest {

	private LocationResponseHandler responseHandler;
	private TopologyServerSynchronizer synchronizer;
	private LocationSender locationUpdater;

	@Before
	public void setUp() {
		synchronizer = mock(TopologyServerSynchronizer.class);
		locationUpdater = mock(LocationSender.class);
		responseHandler = new LocationResponseHandler(synchronizer,
				locationUpdater, 1);
	}

	@Test
	public void testHandleResponseSuccess() throws ClientProtocolException,
			IOException {
		HttpResponse response = mock(HttpResponse.class);
		HttpEntity entity = mock(HttpEntity.class);
		when(entity.getContent()).thenReturn(new StringBufferInputStream("asdf"));
		when(response.getEntity()).thenReturn(entity);
		StatusLine statusline = mock(StatusLine.class);
		// 201
		when(statusline.getStatusCode()).thenReturn(201);
		when(response.getStatusLine()).thenReturn(statusline);
		responseHandler.handleResponse(response);
		verify(synchronizer).syncServerRecord();
	}
	
	@Test
	public void testHandleResponseMoreDetail() throws ClientProtocolException,
			IOException {
		HttpResponse response = mock(HttpResponse.class);
		HttpEntity entity = mock(HttpEntity.class);
		when(entity.getContent()).thenReturn(new StringBufferInputStream("asdf"));
		when(response.getEntity()).thenReturn(entity);
		StatusLine statusline = mock(StatusLine.class);
		// 300
		when(statusline.getStatusCode()).thenReturn(300);
		when(response.getStatusLine()).thenReturn(statusline);
		responseHandler.handleResponse(response);
		verify(synchronizer, never()).syncServerRecord();
		verify(synchronizer).unSyncServerRecord();
		verify(locationUpdater).postLocation(anyInt());
	}

	@Test
	public void testHandleResponseMFail() throws ClientProtocolException,
			IOException {
		HttpResponse response = mock(HttpResponse.class);
		HttpEntity entity = mock(HttpEntity.class);
		when(entity.getContent()).thenReturn(new StringBufferInputStream("asdf"));
		when(response.getEntity()).thenReturn(entity);
		StatusLine statusline = mock(StatusLine.class);
		// 500
		when(statusline.getStatusCode()).thenReturn(500);
		when(response.getStatusLine()).thenReturn(statusline);
		responseHandler.handleResponse(response);
		verify(synchronizer, never()).syncServerRecord();
		verify(synchronizer).unSyncServerRecord();
		verify(locationUpdater, never()).postLocation(anyInt());
	}
}
