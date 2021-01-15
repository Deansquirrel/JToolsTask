package com.yuansong.tools.task.impl;

import java.text.MessageFormat;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.yuansong.tools.common.ObjectManager;
import com.yuansong.tools.task.AbstractDynamicTask;
import com.yuansong.tools.task.IDynamicTaskManager;

@Component
public class DynamicTaskManagerImpl implements IDynamicTaskManager {
	
	@Autowired
	@Qualifier("dynamicTaskSecheduler")
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	private ObjectManager<AbstractDynamicTask> _list;
	
	public DynamicTaskManagerImpl() {
		_list = new ObjectManager<AbstractDynamicTask>();
	}

	@Override
	public void add(AbstractDynamicTask task, String cron) throws Exception {
		synchronized(_list) {
			if(_list.containsKey(task.getName())) {
				throw new Exception(MessageFormat.format("task 【{0}】 is already exists", task.getName()));
			}
			task.start(this.threadPoolTaskScheduler, cron);
			_list.register(task.getName(), task);
		}
	}

	@Override
	public void remove(String name, boolean mayInterruptIfRunning) {
		synchronized(_list) {
			if(_list.containsKey(name)) {
				_list.getObject(name).stop(mayInterruptIfRunning);
				_list.unregister(name);
			}
		}
	}

	@Override
	public void pause(String name, boolean mayInterruptIfRunning) {
		synchronized(_list) {
			if(_list.containsKey(name)) {
				_list.getObject(name).stop(mayInterruptIfRunning);
			}
		}
	}

	@Override
	public void start(String name) throws Exception {
		synchronized(_list) {
			if(!_list.containsKey(name)) {
				throw new Exception(MessageFormat.format("task 【{0}】 is not exists", name));
			}
			AbstractDynamicTask task = _list.getObject(name);
			if(task.getCron() == null || task.getCron() == "") {
				throw new Exception("cron can not be null or empty");
			}
			task.start(this.threadPoolTaskScheduler, task.getCron());
		}
	}

	@Override
	public void start(String name, String cron) throws Exception {
		synchronized(_list) {
			if(!_list.containsKey(name)) {
				throw new Exception(MessageFormat.format("task 【{0}】 is not exists", name));
			}
			_list.getObject(name).start(this.threadPoolTaskScheduler, cron);
		}
	}

	@Override
	public boolean isStarted(String name) {
		if(this.containTask(name)) {
			return this._list.getObject(name).isStarted();
		} else {
			return false;
		}
	}

	@Override
	public boolean isRunning(String name) {
		if(this.containTask(name)) {
			return this._list.getObject(name).isRunning();
		} else {
			return false;
		}
	}

	@Override
	public boolean containTask(String name) {
		return this._list.containsKey(name);
	}

	@Override
	public Set<String> getTaskList() {
		return this._list.keyList();
	}

}
