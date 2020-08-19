package com.yuansong.tools.task;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuansong.tools.common.CommonTool;
import com.yuansong.tools.common.ExceptionTool;

public abstract class AbstractTask implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
	
	protected static final long DEFAULT_TIMEOUT = 1L * 60 * 60;
	
	protected static final String DEFAULT_CRON = "0 0/5 * * * ?";
	
	//是否正在运行
	private boolean running = false;

	@Override
	public void run() {
		//生成任务ID
		String taskId = "";
		
		try {
			taskId = this.createTaskId();			
		} catch(Exception e) {
			logger.error("get taskId error:" + ExceptionTool.getStackTrace(e));
			taskId = CommonTool.UUID();
		}
		try {
			this.prefixExec(taskId);	
		} catch(Exception e) {
			logger.error(MessageFormat.format("prefix exec error: {0} {1} {2}", this.getType(), taskId, ExceptionTool.getStackTrace(e)));
			return;
		}
		
		try {
			TimeoutUtil.process(new Runnable() {
				@Override
				public void run() {
					job();
				}
			}, this.getTimeout());
		}  catch (InterruptedException e) {
			this.handleError(e, taskId);
		} catch (ExecutionException e) {
			this.handleError(e, taskId);
		} catch (TimeoutException e) {
			this.handleError(new RuntimeException("timeout"), taskId);
		} finally {
			try {
				this.suffixExec(taskId);				
			} catch(Exception e) {
				logger.error(MessageFormat.format("suffix exec error: {0} {1} {2}", this.getType(), taskId, ExceptionTool.getStackTrace(e)));
			}
		}
		
//		try {
//			this.job();
//		} catch(Exception e) {
//			this.handleError(e, taskId);
//		} finally {
//			this.suffixExec(taskId);
//		}
		
//		ExecutorService exec = Executors.newSingleThreadExecutor();
//		
//		try {
//			exec.submit(new Runnable() {
//				@Override
//				public void run() {
//					job();
//				}
//			}).get((this.getTimeout() > 0L ? this.getTimeout() : AbstractTask.DEFAULT_TIMEOUT), TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			this.handleError(e, taskId);
//		} catch (ExecutionException e) {
//			this.handleError(e, taskId);
//		} catch (TimeoutException e) {
//			this.handleError(new RuntimeException("timeout"), taskId);
//		} finally {
//			exec.shutdown();
//			try {
//				if(!exec.awaitTermination(1000L, TimeUnit.MILLISECONDS)) {
//					exec.shutdownNow();
//				}
//			} catch (InterruptedException e) {
//				exec.shutdownNow();
//			}
//			this.suffixExec(taskId);
//		}
	}
	
	/**
	 * job错误处理
	 * @param e
	 * @param taskId
	 */
	protected abstract void handleError(Exception e, String taskId);
	
	protected abstract String createTaskId();
	
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
	public abstract long getTimeout();
	
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
	protected void suffixExec(String taskId) {
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
