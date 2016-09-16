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
package ca.phon.app.query;

import java.util.logging.Logger;

import ca.phon.query.script.QueryTask;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;

public class QueryTimer implements PhonTaskListener {
	
	private final static Logger LOGGER = Logger.getLogger(QueryTimer.class.getName());

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
