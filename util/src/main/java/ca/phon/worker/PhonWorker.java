/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.worker.PhonTask.TaskStatus;

/**
 * A worker thread for the application.
 * Tasks are place on the worker thread using
 * the invokeLater(Runnable) method.
 * 
 * Tasks are run FIFO.
 *
 */
public class PhonWorker extends Thread {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonWorker.class.getName());

	/** The worker thread name */
	private static final String STATIC_THREAD_NAME = "PhonWorker-";
	private static volatile int STATIC_THREAD_NUM = 0;
	
	/** The queue */
	private ConcurrentLinkedQueue<Runnable> queue = 
		new ConcurrentLinkedQueue<Runnable>();
	
	/** The static instance */
	private volatile static PhonWorker _instance;
	
	/** The shutdown thread */
	private static PhonWorker _shutdownThread;
	
	/** Is this thread running? */
	private volatile boolean shutdown = false;
	
	private volatile boolean haltOnError = false;
	
	private volatile boolean finishWhenQueueEmpty = false;
	
	/** Task which is run even after shutdown() has been called */
	private Runnable finalTask;
	
	/** Get the static instance */
	public static PhonWorker getInstance() {
		if(_instance == null || !_instance.isAlive())
			_instance = new PhonWorker();
		return _instance;
	}
	
	/**
	 * Creates a new PhonWorker thread and returns
	 * it.  The new thread is not maintained by this
	 * class and the reference to it should be kept and
	 * shutdown by the requsting class.
	 * 
	 * @return a new PhonWorker thread
	 */
	public static PhonWorker createWorker() {
		return new PhonWorker();
	}
	
	/**
	 * Create a new worker using a shared queue.
	 * 
	 * @return a new PhonWorker thread
	 */
	public static PhonWorker createWorker(ConcurrentLinkedQueue<Runnable> queue) {
		PhonWorker retVal = new PhonWorker();
		retVal.queue = queue;
		return retVal;
	}
	
	private static String getNextThreadName() {
		return STATIC_THREAD_NAME + (STATIC_THREAD_NUM++);
	}
	
	/** Constructor */
	protected PhonWorker() {
		super(getNextThreadName());
		
		super.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Logger.getLogger(getClass().getName()).severe("Error in thread '" + getName() + "'");
				Logger.getLogger(getClass().getName()).severe(e.toString());
				e.printStackTrace();
				
				if(finalTask != null) {
					try {
						finalTask.run();
					} catch (Exception ex) {
						Logger.getLogger(getClass().getName()).warning(ex.toString());
					}
				}
			}
			
		});
	}
	
	/**
	 * Add a task to the queue.
	 * 
	 * @param task
	 */
	public void invokeLater(Runnable task) {
		queue.add(task);
	}
	
	@Override
	public void run() {
		shutdown = false;
		LOGGER.fine("Starting worker thread: " + getName());
		while(!shutdown) {
			if(queue.peek() != null) {
				// run the next task in the queue
				Runnable nextTask = queue.poll();
				try {
					nextTask.run();
					
					if(nextTask instanceof PhonTask) {
						PhonTask pt = (PhonTask)nextTask;
						if(pt.getStatus() == TaskStatus.ERROR) {
							if(pt.getException() != null)
								throw pt.getException();
							else
								throw new RuntimeException("Error");
						}
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					if(!haltOnError)
						continue;
					else
						shutdown = true;
				}
			} else {
				if(this != _shutdownThread && !finishWhenQueueEmpty) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						shutdown = true;
						continue;
					}
				} else {
					shutdown = true;
				}
			}
		}
		
		if(finalTask != null) {
			try {
				finalTask.run();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		LOGGER.fine("Worker thread finished: " + getName());
	}
	
	public static PhonWorker getShutdownThread() {
		if(_shutdownThread == null) {
			_shutdownThread = PhonWorker.createWorker();
			_shutdownThread.setName("Phon Shutdown Hook");
		}
		return _shutdownThread;
	}
	
	/**
	 * Shutdown the worker thread.  The currently executing task
	 * must complete first.
	 */
	public void shutdown() {
		shutdown = true;
		LOGGER.fine("Shutdown worker thread: " + getName());
	}
	
	/**
	 * Tells if the currently running thread
	 * is the worker thread.
	 * 
	 * @return true if we are running in the worker thread,
	 * false otherwise
	 */
	public static boolean isStaticWorkerThread() {
		if(_instance == null) return false;
		Thread currentThread = Thread.currentThread();
		if(currentThread.getName().equals(_instance.getName()))
			return true;
		else
			return false;
	}
	
	public boolean hasTasks() {
		return queue.isEmpty();
	}

	public boolean isHaltOnError() {
		return haltOnError;
	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

	public Runnable getFinalTask() {
		return finalTask;
	}

	public void setFinalTask(Runnable finalTask) {
		this.finalTask = finalTask;
	}
	
	public Collection<Runnable> getTasks() {
//		Runnable[] tasks = queue.toArray(new Runnable[0]);
		ArrayList<Runnable> retVal = 
			new ArrayList<Runnable>();
		retVal.addAll(queue);
		
		return retVal;
	}

	public boolean isFinishWhenQueueEmpty() {
		return finishWhenQueueEmpty;
	}

	public void setFinishWhenQueueEmpty(boolean finishWhenQueueEmpty) {
		this.finishWhenQueueEmpty = finishWhenQueueEmpty;
	}
}
