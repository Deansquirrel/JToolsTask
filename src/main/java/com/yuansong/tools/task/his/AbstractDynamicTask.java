package com.yuansong.tools.task.his;

public abstract class AbstractDynamicTask {
	
	private volatile boolean running = false;
	
	public boolean isRunning() {
		return this.running;
	}
}
