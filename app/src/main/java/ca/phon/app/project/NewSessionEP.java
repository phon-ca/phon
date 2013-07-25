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
package ca.phon.app.project;

import java.util.Map;

import javax.swing.JDialog;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;


/**
 * New session module.
 * 
 */
@PhonPlugin(name="default")
public class NewSessionEP implements IPluginEntryPoint {
	
	private Project proj;
	
	private final static String EP_NAME = "NewSession";
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo)  {
		if(initInfo.get("project") == null)
			throw new IllegalArgumentException("Project property not set.");
		
		proj = (Project)initInfo.get("project");
		
		JDialog dlg = null;
		if(initInfo.get("corpus") == null) {
			dlg = new NewSessionDialog(proj);
		} else {
			dlg = new NewSessionDialog(proj, initInfo.get("corpus").toString());
		}

		dlg.setVisible(true);
		while(dlg.isVisible()) {
			try {
				Thread.sleep(500);
			} catch(Exception e) { break; }
		}
	}
}
