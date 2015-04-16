/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class PhonWorkerGroup {
	
	/** The queue of tasks to complete */
	private ConcurrentLinkedQueue<Runnable> tasks;
	
	/** The array of running tasks */
	private PhonWorker[] runningTasks;
	
	/** Shutdown hook */
	private boolean shutdown = false;
	
	/**
	 * Create a new task manager with the specified
	 * number of concurrent tasks.
	 * 
	 * @param taskWindow
	 */
	public PhonWorkerGroup(int taskWindow) {
		super();
		
		this.tasks = new ConcurrentLinkedQueue<Runnable>();
		this.runningTasks = new PhonWorker[taskWindow];
		for(int i = 0; i < taskWindow; i++)
			this.runningTasks[i] = PhonWorker.createWorker(this.tasks);
	}

	/**
	 * Add a new task to the queue.
	 */
	public void queueTask(Runnable t) {
		tasks.add(t);
	}
	
	public void begin() {
		shutdown = false;
		
		// startup all worker threads
		for(int i = 0; i < runningTasks.length; i++)
			runningTasks[i].start();
	}
	
	public void shutdown() {
		shutdown = true;
		
		// tell the worker threads to shutdown
		for(int i = 0; i < runningTasks.length; i++)
			runningTasks[i].shutdown();
	}

	public boolean isShutdown() {
		return shutdown;
	}
	
	public Collection<Runnable> getTaskList() {
		return tasks;
	}
	
	public PhonWorker[] getThreads() {
		return runningTasks;
	}
	
//	public static void main(String[] args) {
//		// make a bunch of long running tasks
//		final PhonWorkerGroup tm = new PhonWorkerGroup(3);
//		
//		final Random r = new Random();
//		for(int i = 0; i < 20; i++) {
//			PhonTask t = new PhonTask() {
//
//				@Override
//				public void performTask() {
//					for(int j = 0; j < r.nextInt(100); j++) {
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException ex) {
//							ex.printStackTrace();
//						}
//					}
//				}
//				
//			};
//			tm.queueTask(t);
//		}
//		
//		JFrame frame = new JFrame();
//		
//		JButton startBtn = new JButton("Start Group");
//		startBtn.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				tm.begin();
//			}
//			
//		});
//		
//		JButton stopBtn = new JButton("Shutdown Group");
//		stopBtn.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				tm.shutdown();
//			}
//			
//		});
//		
//		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
//		frame.getContentPane().add(startBtn);
//		frame.getContentPane().add(stopBtn);
//		
//		frame.pack();
//		frame.setVisible(true);
////		tm.start();
//	}
}
