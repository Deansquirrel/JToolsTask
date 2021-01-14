package com.yuansong.tools.task;

import java.util.Set;

public interface IDynamicTaskManager {

	/**
	 * 添加并启动任务
	 * @param task
	 * @throws Exception 
	 */
	public void add(AbstractDynamicTask task, String cron) throws Exception;
	
	/**
	 * 移除任务
	 * @param name
	 */
	public void remove(String name);
	
	/**
	 * 暂停任务
	 * @param name
	 */
	public void pause(String name);
	
	/**
	 * 恢复任务
	 * @param name
	 * @throws Exception 
	 */
	public void start(String name) throws Exception;
	
	public void start(String name, String cron) throws Exception;
	
	/**
	 * 是否已启动
	 * @param name
	 * @return
	 */
	public boolean isStarted(String name);
	
	/**
	 * 任务是否运行中
	 * @param name
	 * @return
	 */
	public boolean isRunning(String name);
	
	/**
	 * 是否包含任务
	 * @param name
	 * @return
	 */
	public boolean containTask(String name);
	
	/**
	 * 获取任务关键字清单
	 * @return
	 */
	public Set<String> getTaskList();
}
