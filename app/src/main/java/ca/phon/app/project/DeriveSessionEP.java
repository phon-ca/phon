/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.mergewizard.DeriveSessionWizard;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Creates a new session from the records found in
 * one or more current sessions.
 */
@PhonPlugin(name="default")
public class DeriveSessionEP implements IPluginEntryPoint {

	private final static String EP_NAME = "DeriveSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		// get project
		final EntryPointArgs args = new EntryPointArgs(initInfo);
		final Project project = args.getProject();
		
		if(project != null) {
			final Runnable onEdt = new Runnable() {
				public void run() {
					final DeriveSessionWizard wizard = new DeriveSessionWizard(project);
					wizard.setParentFrame(CommonModuleFrame.getCurrentFrame());
					wizard.setSize(600, 500);
					wizard.setLocationByPlatform(true);
					wizard.setVisible(true);
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				onEdt.run();
			else
				SwingUtilities.invokeLater(onEdt);
		}
	}

}
