package com.yuansong.tools.task.his;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class DynamicTaskManagerHis {
	
	private static final Logger logger = LoggerFactory.getLogger(DynamicTaskManagerHis.class);

	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	//任务缓存
	private Map<String, ScheduledFuture<?>> futureMap;
	
	//对象缓存
	private Map<String, AbstractTask> taskMap;
	
	//运行时间缓存
	private Map<String, String> cronMap;
	
	public DynamicTaskManagerHis() {
		futureMap = new ConcurrentHashMap<String, ScheduledFuture<?>>();
		taskMap = new ConcurrentHashMap<String, AbstractTask>();
		cronMap = new ConcurrentHashMap<String, String>();
	}
	
	@Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(10);
		threadPoolTaskScheduler.setThreadNamePrefix("dynamicTask-");
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        return threadPoolTaskScheduler;
    }
	
	/**
	 * 添加任务配置
	 * @param task
	 */
	public void addTaskConfig(AbstractTask task) {
		String type = task.getType();
		if(this.taskMap.containsKey(type)) {
			this.removeTaskConfig(type);
		}
		this.taskMap.put(type , task);
		logger.info("task config added [{}]",task.getType());
	}
	
	/**
	 * 移除任务配置
	 * @param type
	 */
	public void removeTaskConfig(String type) {
		if(this.futureMap.containsKey(type)) {
			this.stopTask(type);
		}
		this.taskMap.remove(type);
		logger.info("task config removed [{}]",type);
	}
	
	/**
	 * 启动任务
	 * @param type
	 * @throws Exception
	 */
	public synchronized void startTask(String type) throws Exception {
		if(!this.taskMap.containsKey(type)) {
			throw new Exception(MessageFormat.format("Task Cofig missing [{0}]", type));
		}
		if(this.futureMap.containsKey(type)) {
			this.stopTask(type);
		}
		AbstractTask task = this.taskMap.get(type);
		String cron = task.getDefaultCron();
		if(this.isCronValid(task.getCron())) {
			cron = task.getCron();
		} else {
			if(!this.isCronValid(cron)) {
				cron = AbstractTask.DEFAULT_CRON;
			}
		}
		ScheduledFuture<?> future = this.threadPoolTaskScheduler.schedule(task, new CronTrigger(cron));
		
		if(future != null) {
			this.futureMap.put(task.getType(), future);
			this.cronMap.put(task.getType(), cron);
		} else {
			throw new Exception(MessageFormat.format("Task Add error {0}", task.getType()));
		}
		logger.info("task start [{} {}]", type, cron);
	}
	
	/**
	 * 停止任务
	 * @param type 任务类型
	 */
	public synchronized void stopTask(String type){
		if(!this.futureMap.containsKey(type)) return;
		ScheduledFuture<?> future = this.futureMap.get(type);
		if(future != null) {
			future.cancel(false);
		}
		this.futureMap.remove(type);
		this.cronMap.remove(type);
		logger.info("task stop [{}]", type);
	}
	
	/**
	 * 测试cron公式是否有效
	 * @param cron
	 * @return
	 */
	public boolean isCronValid(String cron) {		
		logger.debug("isCronValid " + cron);
		ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
		ScheduledFuture<?> future = null;
		try{
			pool.initialize();
			future = pool.schedule(new Runnable() {
				@Override
				public void run() {
					logger.debug("job cron: {}", cron);
				}
			}, new CronTrigger(cron));
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(future != null) {
				future.cancel(true);
			}
			pool.destroy();
		}
		return true;
	}
	
	/**
	 * 任务状态
	 * @param type
	 * @return
	 */
	public boolean isTaskRunning(String type) {
		if(this.taskMap.containsKey(type) && this.futureMap.containsKey(type)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取任务运行所使用的cron
	 * @param type
	 * @return
	 */
	public String getCurrCron(String type) {
		String cron = "";
		if(this.cronMap.containsKey(type)) {
			cron = this.cronMap.get(type);
		}
		return cron;
	}
	
}
