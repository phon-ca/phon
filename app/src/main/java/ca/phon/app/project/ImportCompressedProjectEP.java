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

import java.io.File;
import java.util.Map;

import ca.phon.gui.CommonModuleFrame;
import ca.phon.system.plugin.IPluginEntryPoint;
import ca.phon.system.plugin.PhonPlugin;
import ca.phon.util.NativeDialogs;
import ca.phon.util.PhonUtilities;

/**
 * Import a project for a .phon/.zip file.
 * 
 * If project is version 1.3, project is also upgraded.
 * 
 */
@PhonPlugin(name="default")
public class ImportCompressedProjectEP implements IPluginEntryPoint {
	
	private final static String PROJ_FILE_KEY = "projectfile";

	private final static String EP_NAME = "ImportCompressedProject";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		// get the input project file
		if(initInfo.get(PROJ_FILE_KEY) == null) {
			// show import dialog...
			NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null,
					"No input file", "Input file must not be null.");
			return;
		} else {
			String inputFilePath = (String)initInfo.get(PROJ_FILE_KEY);
			File inputFile = new File(inputFilePath);
			
			// make sure files exists
			if(!inputFile.exists()) {
				NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null,
						"File not found", "File: " + inputFile.getAbsolutePath());
				return;
			}
			
			// make sure files ends with .phon or .zip
			boolean isCompressedFile = 
				inputFile.getName().toLowerCase().endsWith(".phon") || inputFile.getName().toLowerCase().endsWith(".zip");
			if(!isCompressedFile) {
				NativeDialogs.showMessageDialogBlocking(CommonModuleFrame.getCurrentFrame(), null, "Invalid file", "Phon can only decompress .phon or .zip files.");
				return;
			}
			
			// extract zip contents to workspace directory
			File workspaceFile = PhonUtilities.getPhonWorkspace();
			
		}
	}
}
