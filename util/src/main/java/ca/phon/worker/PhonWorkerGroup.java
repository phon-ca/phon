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
