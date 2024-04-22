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
package ca.phon.plugin;

import ca.phon.worker.PhonTask;

import java.util.*;

public abstract class AbstractPluginEntryPoint extends PhonTask implements IPluginEntryPoint {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(AbstractPluginEntryPoint.class.getName());
	
	/**
	 * The performTask method calls pluginStart. This property
	 * holds the map of arguments sent to plugin start when
	 * performTask is called.
	 */
	public static final String PLUGIN_START_ARGS = "_pluginStart_args_";

	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		
		Map<String, Object> args = new HashMap<String, Object>();
		Object o = super.getProperty(PLUGIN_START_ARGS);
		if(o != null && o instanceof Map<?, ?>) {
			args = (Map<String, Object>)o;
		}
		
		try {
			pluginStart(args);
			super.setStatus(TaskStatus.FINISHED);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
		}
		
	}

}
