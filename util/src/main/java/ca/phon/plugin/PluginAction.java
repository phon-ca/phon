/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

/**
 * Action for quickly executing plugin entry points.
 *
 */
public class PluginAction extends AbstractAction {
	
	private static final long serialVersionUID = 7414527209391736686L;

	private final static Logger LOGGER = Logger.getLogger(PluginAction.class.getName());
	
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
				LOGGER.warning(pe.toString());
				pe.printStackTrace();
			}
		}
	}

}
