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
package ca.phon.app.query;

import ca.phon.query.script.*;
import ca.phon.ui.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.*;

public class QueryTimer implements PhonTaskListener {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryTimer.class.getName());

	QueryTask tasks[];
	private long startTimeMS;
	private Integer tasksCompleted = 0;
	private PhonLoggerConsole console = null;
	
	public QueryTimer(QueryTask tasks[], long startTimeMS, PhonLoggerConsole console) {
		super();
		
		this.tasks = tasks;
		this.startTimeMS = startTimeMS;
		this.console = console;
	}
	
	private void printQueryInfo() {
		long finishedTimeMS = System.currentTimeMillis();
		int queryTimeMS = (int)(finishedTimeMS - startTimeMS);
		
		LOGGER.info("--------------------");
		
		int numCompleted = 0;
		int numError = 0;
		int numTerminated = 0;
		for(int i = 0; i < tasks.length; i++) {
			QueryTask t = tasks[i];
			if(t.getStatus() == TaskStatus.ERROR)
				numError++;
			else if(t.getStatus() == TaskStatus.FINISHED)
				numCompleted++;
			else if(t.getStatus() == TaskStatus.TERMINATED)
				numTerminated++;
		}
		
		String line = "Finished: " +
			(numCompleted + " Completed") +
			(", " + numError + " Errors") + 
			(numTerminated > 0 ? ", " + numTerminated + " Terminated" : "");
		LOGGER.info(line);
		LOGGER.info("Query time: " + queryTimeMS);
	}
	
	@Override
	public void propertyChanged(PhonTask task, String property,
			Object oldValue, Object newValue) {
		
	}

	@Override
	public void statusChanged(PhonTask task, TaskStatus oldStatus,
			TaskStatus newStatus) {
		
		if( newStatus != oldStatus &&
				(newStatus != TaskStatus.RUNNING && newStatus != TaskStatus.WAITING)) {
			synchronized(tasksCompleted) {
				tasksCompleted++;
				
				if(tasksCompleted == tasks.length) {
					printQueryInfo();
					System.gc();
				}
			}
		}
	}
	
}
