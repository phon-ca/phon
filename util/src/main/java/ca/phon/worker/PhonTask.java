/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.worker;

import java.util.concurrent.*;

import javax.swing.event.*;

/**
 *
 */
public abstract class PhonTask implements Runnable {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonTask.class.getName());

	/*
	 * Common properties
	 *
	 */
	/**
	 * Task progress property
	 * Float
	 * Percentage done [0.0-1.0] or < 0 if inderterminate
	 */
	public static final String PROGRESS_PROP = "_task_progress_";
	
	/**
	 * Task status property.
	 * Holds current status test (optional)
	 *
	 */
	public static final String STATUS_PROP = "_task_status_";
	
	public static enum TaskStatus {
		WAITING,	// waiting to start
		RUNNING,	// running
		TERMINATED, // terminated
		FINISHED, 	// normal completion
		ERROR		// the task finished due to an exception
	};
	
	private static final String taskPrefix = "PhonTask #";
	private static volatile int numTasks = 0;
	
	/** The task status */
	private volatile TaskStatus status = TaskStatus.WAITING;
	
	/** If an exception was caught during the run method, it's stored here */
	protected Exception err = null;
	
	/** The start time (in ms) of the task */
	private long startTime = 0L;
	
	/** The end time of the task */
	private long endTime = 0L;
	
	/** The shutdown hook */
	private volatile boolean shutdown = false;
	
	private String taskName = "";
	
	/** Listeners */
	private EventListenerList listeners = new EventListenerList();
	
	/** Props */
	protected ConcurrentHashMap<String, Object> props = new 
		ConcurrentHashMap<String, Object>();
	
	public PhonTask() {
		this(null);
	}
	
	public PhonTask(String taskName) {
		super();
		
		numTasks++;
		
		if(taskName == null) {
			this.taskName = taskPrefix + numTasks;
		} else {
			this.taskName = taskName;
		}
		
		// setup status prop
		props.put(STATUS_PROP, "");
		
		// setup progrss prop
		props.put(PROGRESS_PROP, new Float(0.0f));
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public long getRunTime() {
		return endTime - startTime;
	}
	
	@Override
	public void run() {
		startTime = System.currentTimeMillis();
		setStatus(TaskStatus.RUNNING);
		try {
			performTask();
		} catch(Exception e) {
			LOGGER.error( e.getMessage(), e);
			err = e;
			setStatus(TaskStatus.ERROR);
		}
		endTime = System.currentTimeMillis();

		// tasks will need to call finished
		// when they need
//		if(status == TaskStatus.RUNNING)
//			setStatus(TaskStatus.FINISHED);
	}
	
	/**
	 * The abstract run method.  This method must be implemented
	 * by all subclasses.
	 */
	public abstract void performTask();
	
	public synchronized void shutdown() {
		shutdown = true;
		setStatus(TaskStatus.TERMINATED);
	}

	public synchronized boolean isShutdown() {
		return shutdown;
	}
	
	protected void setStatus(TaskStatus s) {
		TaskStatus oldStatus = this.status;
		this.status = s;
		fireStatusChange(oldStatus, s);
	}
	
	public TaskStatus getStatus() {
		return this.status;
	}
	
	public String getName() {
		return this.taskName;
	}
	
	public void setName(String name) {
		this.taskName = name;
	}
	
	public Exception getException() {
		return err;
	}
	
	public Object getProperty(String prop) {
		return props.get(prop);
	}
	
	protected void setProperty(String prop, Object val) {
		Object oldVal = getProperty(prop);
		props.put(prop, val);
		
		firePropertyChange(prop, oldVal, val);
	}
	
	public void addTaskListener(PhonTaskListener l) {
		listeners.add(PhonTaskListener.class, l);
	}
	
	public void removeTaskListener(PhonTaskListener l) {
		listeners.remove(PhonTaskListener.class, l);
	}
	
	protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
		if(oldValue == newValue) return;
		
		for(PhonTaskListener l:listeners.getListeners(PhonTaskListener.class)) {
			l.propertyChanged(this, prop, oldValue, newValue);
		}
	}
	
	protected void fireStatusChange(TaskStatus oldStatus, TaskStatus newStatus) {
		if(oldStatus == newStatus) return;
		
		for(PhonTaskListener l:listeners.getListeners(PhonTaskListener.class)) {
			l.statusChanged(this, oldStatus, newStatus);
		}
	}
}
