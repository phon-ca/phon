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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Action for quickly executing plugin entry points.
 *
 */
public class PluginAction extends AbstractAction {
	
	private static final long serialVersionUID = 7414527209391736686L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PluginAction.class.getName());
	
	/**
	 * entry point id
	 */
	private String epId;
	
	private Map<String, Object> args = new HashMap<String, Object>();
	
	private boolean runInBackground = false;
	
	/**
	 * Constructor
	 * 
	 * @param ep ID of the plugin entry point
	 */
	public PluginAction(String ep) {
		this(ep, false);
	}
	
	public PluginAction(String ep, boolean runInBackground) {
		this.epId = ep;
		this.runInBackground = runInBackground;
	}
	
	public void putArg(String name, Object val) {
		args.put(name, val);
	}
	
	public void putArgs(Map<String, Object> args) {
		this.args.putAll(args);
	}
	
	public void removeArg(String name) {
		args.remove(name);
	}
	
	public void clearArgs() {
		args.clear();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(runInBackground) {
			PluginEntryPointRunner.executePluginInBackground(epId, args);
		} else {
			try {
				PluginEntryPointRunner.executePlugin(epId, args);
			} catch (PluginException pe) {
				LOGGER.error( pe.getMessage(), pe);
			}
		}
	}

}
