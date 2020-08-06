package com.yuansong.tools.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTask implements Runnable {
	
	protected static final long DEFAULT_TIMEOUT = 60 * 60 * 12;
	
	protected static final String DEFAULT_CRON = "0 0/5 * * * ?";
	
	//是否正在运行
	private boolean running = false;

	@Override
	public void run() {		
		//生成任务ID
		String taskId = this.getTaskId();
		
		this.prefixExec(taskId);
		
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<?> f = exec.submit(new Runnable() {
			@Override
			public void run() {
				job();
			}
		});
		
		String content = "";
		try {
			f.get(this.getTimeout() > 0 ? this.getTimeout() : AbstractTask.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
			content = "complete";
		} catch (Exception e) {
			this.handleError(e, taskId);
		} finally {
			this.suffixExec(taskId, content);
		}
	}
	
	/**
	 * job错误处理
	 * @param e
	 * @param taskId
	 */
	protected abstract void handleError(Exception e, String taskId);
	
	protected abstract String getTaskId();
	
	/**
	 * Task类型（相同类型任务的唯一标记）
	 * @return 获取Task类型
	 */
	public abstract String getType();
	
	/**
	 * 运行时间
	 * @return 获取运行时间
	 */
	public abstract String getCron();
	
	/**
	 * Task服务内容
	 */
	public abstract void job();

	/**
	 * 单次任务超时时间（秒）
	 * @return
	 */
	public abstract int getTimeout();
	
	/**
	 * 任务默认运行时间，当配置的时间无效时使用
	 * @return 任务默认运行时间
	 */
	protected String getDefaultCron() {
		return "0 0/5 * * * ?";
	}
	
	/**
	 * 任务前置执行内容
	 * @param taskId 任务执行ID
	 */
	protected void prefixExec(String taskId) {
		this.running = true;
	}
	
	/**
	 * 任务后置执行内容
	 * @param taskId 任务执行ID
	 */
	protected void suffixExec(String taskId, String content) {
		this.running = false;
	}
	/**
	 * 
	 * @return 是否处于运行状态
	 */
	public boolean isRunning() {
		return this.running;
	}
}
