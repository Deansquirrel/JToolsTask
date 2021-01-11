package com.yuansong.tools.task;

public interface IDynamicTaskManager {

	/**
	 * 添加任务
	 * @param task
	 */
	public void addTask(IDynamicTask task);
	
	/**
	 * 移除任务
	 * @param name
	 */
	public void removeTask(String name);
	
	/**
	 * 暂停任务
	 * @param name
	 */
	public void pauseTask(String name);
	
	/**
	 * 恢复任务
	 * @param name
	 */
	public void resumeTask(String name);
	
	/**
	 * 任务是否运行中
	 * @param name
	 * @return
	 */
	public boolean isTaskRunning(String name);
	
	/**
	 * 获取任务
	 * @param name
	 * @return
	 */
	public String getTask(String name);
	
	/**
	 * 是否包含任务
	 * @param name
	 * @return
	 */
	public boolean containTask(String name);
	
}
