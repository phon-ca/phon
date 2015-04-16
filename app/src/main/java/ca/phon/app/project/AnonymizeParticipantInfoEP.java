/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.project;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;

/**
 * Entry point for module to strip participant information.
 *
 */
@PhonPlugin(name="default")
public class AnonymizeParticipantInfoEP implements IPluginEntryPoint {

	public final static String EP_NAME = "AnonymizeParticipantInformation";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs info = new EntryPointArgs(args);
		final Project project = info.getProject();
		
		if(project == null) return;
		
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final AnonymizeParticipantInfoWizard wizard = new AnonymizeParticipantInfoWizard(project);
				wizard.setSize(500, 600);
				wizard.centerWindow();
				wizard.showWizard();
			}
			
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
