package com.yuansong.tools.task;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.yuansong.tools.common.CommonTool;
import com.yuansong.tools.common.ExceptionTool;

public abstract class AbstractDynamicTask implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractDynamicTask.class);
	
	private long _lastStart = 0L;
	private long _lastEnd = 0L;
	private boolean _running = false;
	private String _cron = "";
	
	private ScheduledFuture<?> _future = null;
				
	/**
	 * 任务名称
	 * @return
	 */
	public abstract String getName();
			
	/**
	 * 之前前处理
	 */
	protected abstract void prefixJob();
	
	/**
	 * 执行内容
	 */
	protected abstract void job();
	
	/**
	 * 执行后处理
	 */
	protected abstract void suffixJob();
	
	public String getCron() {
		return _cron;
	}	

	/**
	 * 获取最后启动时间戳
	 * @return
	 */
	public long getLastStart() {
		return _lastStart;
	};
	
	/**
	 * 获取最后完成时间戳
	 * @return
	 */
	public long getLastEnd() {
		return _lastEnd;
	}
	
	/**
	 * 是否已启动
	 * @return
	 */
	public boolean isStarted() {
		return this.getScheduledFuture() != null;
	}
	
	/**
	 * 是否运行中
	 * @return
	 */
	public boolean isRunning() {
		return _running;
	};
	
	/**
	 * 获取任务调度（null=无任务调度）
	 * @return
	 */
	protected ScheduledFuture<?> getScheduledFuture() {
		return _future;
	}
	
	/**
	 * 启动
	 * @throws Exception
	 */
	public synchronized void start(ThreadPoolTaskScheduler pool, String cron) throws Exception {
		if(this._future != null) {
			throw new Exception(MessageFormat.format("task scheduled is already exist 【{0}】 【{1}】", this.getName(), this._cron));
		}
		this._future = pool.schedule(this, new CronTrigger(cron));
		this._cron = cron;
		logger.info(MessageFormat.format("task 【{0}】 is scheduled with cron 【{1}】", this.getName(), this._cron));
	}
	
	/**
	 * 停止
	 */
	public synchronized void stop() {
		if(this._future != null) {
			this._future.cancel(false);
			this._future = null;
		}
	}

	@Override
	public void run() {
		//==============================================
		//如运行中，则跳过执行
		if(this.isRunning()) {
			return;
		}
		//==============================================
		this._lastStart = (new Date()).getTime();
		this._running = true;
		//==============================================
		String taskId = CommonTool.UUID();
		logger.info(MessageFormat.format("start task 【{0}】【{1}】", this.getName(), taskId));
		try {
			this.prefixJob();
			this.job();
			this.suffixJob();
		} catch(Exception e) {
			logger.error(MessageFormat.format("task 【{0}】【{1}】 error: {2}", this.getName(), taskId, e.getMessage()));
			logger.error(ExceptionTool.getStackTrace(e));
		}
		logger.info(MessageFormat.format("task 【{0}】【{1}】 findshed", this.getName(), taskId));
		//==============================================
		this._running = false;
		this._lastEnd = (new Date()).getTime();
	}
	
}
