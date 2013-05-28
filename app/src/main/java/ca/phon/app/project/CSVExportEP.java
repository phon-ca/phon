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

import ca.phon.application.project.IPhonProject;
import ca.phon.phon2csv.wizard.CSVExportWizard;
import ca.phon.system.plugin.IPluginEntryPoint;
import ca.phon.system.plugin.PhonPlugin;

@PhonPlugin(name="default")
public class CSVExportEP implements IPluginEntryPoint {

	private final static String EP_NAME = "CSVExport";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		// display CSV import wizard
		if(initInfo.get("project") != null) {
			IPhonProject project = (IPhonProject)initInfo.get("project");
			
			CSVExportWizard wizard = new CSVExportWizard();
			wizard.setProject(project);
			wizard.showWizard();
		} else {
			throw new IllegalArgumentException("No project given");
		}
	}
}
