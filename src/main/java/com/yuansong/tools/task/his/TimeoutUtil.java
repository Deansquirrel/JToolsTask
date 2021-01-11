package com.yuansong.tools.task.his;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TimeoutUtil {
	
	private static ThreadPoolTaskScheduler threadPoolTaskScheduler = null;
	
	public static void process(Runnable task, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		if(threadPoolTaskScheduler == null) {
			threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
			threadPoolTaskScheduler.setPoolSize(10);
			threadPoolTaskScheduler.setThreadNamePrefix("timeoutTask-");
			threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
			threadPoolTaskScheduler.initialize();
		}
		if(task == null) return;
		
		Future<?> future = threadPoolTaskScheduler.submit(task);
		future.get(timeout, TimeUnit.SECONDS);	
	}
}
