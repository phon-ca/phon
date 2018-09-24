/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.app.log.LogManager;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

public class PhonShutdownThread extends PhonWorker {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonShutdownThread.class.getName());
	
	private static PhonShutdownThread _instance = null;
	
	public static PhonShutdownThread getInstance() {
		if(_instance == null) {
			_instance = new PhonShutdownThread();
		}
		return _instance;
	}
	
	private PhonShutdownThread() {
		super();
		setName("Shutdown Hook");
		super.setFinishWhenQueueEmpty(true);
		super.invokeLater(new PhonShutdownTask());
	}

	private class PhonShutdownTask extends PhonTask {

		@Override
		public void performTask() {
			setStatus(TaskStatus.RUNNING);
			
			
			final List<IPluginExtensionPoint<PhonShutdownHook>> shutdownHooksPts = 
					PluginManager.getInstance().getExtensionPoints(PhonShutdownHook.class);
			for(IPluginExtensionPoint<PhonShutdownHook> shutdownHookPt:shutdownHooksPts) {
				final PhonShutdownHook hook = shutdownHookPt.getFactory().createObject();
				try {
					hook.shutdown();
				} catch (PluginException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			// shutdown logger
			LogManager.getInstance().shutdownLogging();
			
			
			setStatus(TaskStatus.FINISHED);
			
			// ensure the JVM exits!
			Runtime.getRuntime().halt(0);
		}
		
	}
	
}
