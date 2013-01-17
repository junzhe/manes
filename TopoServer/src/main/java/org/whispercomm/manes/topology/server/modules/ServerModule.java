package org.whispercomm.manes.topology.server.modules;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.whispercomm.manes.topology.server.TopoServer;
import org.whispercomm.manes.topology.server.manager.TopologyManager;
import org.whispercomm.manes.topology.server.manager.TopologyManagerDefault;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Registers the {@link TopoServer} with Guice as application singletons.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 * 
 */
public class ServerModule extends AbstractModule {

	static final int EXECUTOR_CORE_POOL_SIZE_DEFAULT = 16;
	static final int EXECUTOR_MAX_POOL_SIZE_DEFAULT = 128;
	static final long EXECUTOR_KEEP_ALIVE_TIME = 1000;
	static final TimeUnit EXECUTOR_TIME_UNIT = TimeUnit.MILLISECONDS;
	static final int EXECUTOR_QUEUE_CAPACITY = 10;

	@Override
	protected void configure() {
		bind(TopoServer.class).in(Singleton.class);
		bind(TopologyManager.class).to(TopologyManagerDefault.class).in(
				Singleton.class);
		bind(ExecutorService.class).annotatedWith(Names.named("topoExe"))
				.to(ThreadPoolExecutor.class).in(Singleton.class);
		}

	/**
	 * Constructor of {@link ThreadPoolERxecutor} with default settings.
	 * 
	 * @return
	 */
	@Provides
	ThreadPoolExecutor provideThreadPoolExecutor() {
		return new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE_DEFAULT,
				EXECUTOR_MAX_POOL_SIZE_DEFAULT, EXECUTOR_KEEP_ALIVE_TIME,
				EXECUTOR_TIME_UNIT, new ArrayBlockingQueue<Runnable>(
						EXECUTOR_QUEUE_CAPACITY));
	}

}
