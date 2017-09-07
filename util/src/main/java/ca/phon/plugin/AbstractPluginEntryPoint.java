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
package ca.phon.plugin;

import java.util.*;
import java.util.logging.Logger;

import ca.phon.worker.PhonTask;

public abstract class AbstractPluginEntryPoint extends PhonTask implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(AbstractPluginEntryPoint.class.getName());
	
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
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
		}
		
	}

}
