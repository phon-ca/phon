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

import ca.phon.app.project.checkwizard.CheckWizard;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;

@PhonPlugin(name="default")
public class CheckIPAEP implements IPluginEntryPoint {

	@Override
	public String getName() {
		return "CheckIPA";
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		if(args.get("project") != null) {
			final Project project = (Project)args.get("project");
			
			final CheckWizard cw = new CheckWizard(project);
			cw.setSize(600, 500);
			cw.setLocationByPlatform(true);
			cw.setVisible(true);
		}
	}

}
