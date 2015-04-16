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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionWriter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;

@PhonPlugin(name="default")
public class ExportSessionEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(ExportSessionEP.class.getName());

	private final static String EP_NAME = "ExportSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo)  {
		Project project = null;
		String corpus = null;
		String session = null;
		
		if(initInfo.get("project") == null ||
				initInfo.get("corpusName") == null ||
				initInfo.get("sessionName") == null) {
			throw new IllegalArgumentException("Not enough information to export.");
		}
		
		project = (Project)initInfo.get("project");
		corpus = (String)initInfo.get("corpusName");
		session = (String)initInfo.get("sessionName");
		
		String saveTo = null;
		if(initInfo.get("saveTo") == null) {
			final SaveDialogProperties props = new SaveDialogProperties();
			props.setRunAsync(false);
			props.setFileFilter(FileFilter.xmlFilter);
			props.setCanCreateDirectories(true);
			props.setTitle("Save Session to File");
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			
			saveTo = NativeDialogs.showSaveDialog(props);
			if(saveTo == null) return;
		} else {
			saveTo = initInfo.get("saveTo").toString();
		}

		try {
			// read transcript from the project and save in pretty format
			final Session transcript = project.openSession(corpus, session);
			
			final SessionOutputFactory outputFactory = new SessionOutputFactory();
			final SessionWriter writer = outputFactory.createWriter();
			
			writer.writeSession(transcript, new FileOutputStream(saveTo));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
