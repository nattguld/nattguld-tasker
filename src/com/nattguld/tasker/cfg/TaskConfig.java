package com.nattguld.tasker.cfg;

import com.nattguld.data.cfg.Config;
import com.nattguld.data.cfg.ConfigManager;
import com.nattguld.data.json.JsonReader;
import com.nattguld.data.json.JsonWriter;
import com.nattguld.tasker.TaskManager;

/**
 * 
 * @author randqm
 *
 */

public class TaskConfig extends Config {
	
	/**
	 * Whether to debug or not.
	 */
	private boolean debug;
	
	/**
	 * Whether to remove failed tasks or not.
	 */
	private boolean removeFailed = true;
	
	/**
	 * The maximum amount of parallel tasks allowed.
	 */
	private int maxParallel = 20 * Runtime.getRuntime().availableProcessors();
	
	/**
	 * The maximum queue size.
	 */
	private int maxQueueSize = 100;


	@Override
	protected void read(JsonReader reader) {
		this.debug = reader.getAsBoolean("debug", false);
		this.removeFailed = reader.getAsBoolean("remove_failed", true);
		this.maxParallel = reader.getAsInt("max_parallel", 20 * Runtime.getRuntime().availableProcessors());
		this.maxQueueSize = reader.getAsInt("max_queue_size", 100);
	}

	@Override
	protected void write(JsonWriter writer) {
		writer.write("debug", debug);
		writer.write("remove_failed", removeFailed);
		writer.write("max_parallel", maxParallel);
		writer.write("max_queue_size", maxQueueSize);
	}
	
	@Override
	protected String getSaveFileName() {
		return ".task_config";
	}
	
	/**
	 * Modifies whether to debug or not.
	 * 
	 * @param debug The new state.
	 * 
	 * @return The config.
	 */
	public TaskConfig setDebug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	/**
	 * Retrieves whether to debug or not.
	 * 
	 * @return The result.
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * Modifies whether to remove failed tasks or not.
	 * 
	 * @param removeFailed The new state.
	 * 
	 * @return The config.
	 */
	public TaskConfig setRemoveFailed(boolean removeFailed) {
		this.removeFailed = removeFailed;
		return this;
	}
	
	/**
	 * Retrieves whether to remove failed tasks or not.
	 * 
	 * @return The result.
	 */
	public boolean isRemoveFailed() {
		return removeFailed;
	}
	
	/**
	 * Modifies the maximum amount of parallel tasks allowed.
	 * 
	 * @param maxParallel The new amount.
	 * 
	 * @return The config.
	 */
	public TaskConfig setMaxParallel(int maxParallel) {
		this.maxParallel = maxParallel;
		TaskManager.updateMaxParallel(maxParallel);
		return this;
	}
	
	/**
	 * Retrieves the maximum amount of parallel tasks allowed.
	 * 
	 * @return The amount.
	 */
	public int getMaxParallel() {
		return maxParallel;
	}
	
	/**
	 * Modifies the maximum queue size.
	 * 
	 * @param maxQueueSize The new maximum queue size.
	 * 
	 * @return The config.
	 */
	public TaskConfig setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		return this;
	}
	
	/**
	 * Retrieves the maximum queue size.
	 * 
	 * @return The maximum queue size.
	 */
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	
	/**
	 * Retrieves the config.
	 * 
	 * @return The config.
	 */
	public static TaskConfig getConfig() {
		return (TaskConfig)ConfigManager.getConfig(new TaskConfig());
	}

}
