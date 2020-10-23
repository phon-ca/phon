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
package ca.phon.app;

import java.util.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.plugin.*;
import ca.phon.worker.*;

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
					LOGGER.error( e.getLocalizedMessage(), e);
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
