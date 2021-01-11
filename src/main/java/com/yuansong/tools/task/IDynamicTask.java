package com.yuansong.tools.task;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuansong.tools.common.CommonTool;
import com.yuansong.tools.common.ExceptionTool;

public interface IDynamicTask extends Runnable {
	
	/**
	 * 任务名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 执行时间
	 * @return
	 */
	public String getCron();
	
	/**
	 * 执行内容
	 */
	public void job();
	
	/**
	 * 之前前处理
	 */
	public void prefixJob();
	
	/**
	 * 执行后处理
	 */
	public void suffixJob();
	
	/**
	 * 生成任务ID
	 * @return
	 */
	default String getTaskId() {
		return CommonTool.UUID();
	}

	@Override
	default void run() {
		Logger logger = LoggerFactory.getLogger(IDynamicTask.class);
		String taskId = this.getTaskId();
		logger.info(MessageFormat.format("start task {0} {1}", this.getName(), taskId));
		try {
			this.prefixJob();
			this.job();
			this.suffixJob();			
		} catch(Exception e) {
			logger.error(ExceptionTool.getStackTrace(e));
		}
		MessageFormat.format("task {0} {1} findshed", this.getName(), taskId);
	}
	
	

}
