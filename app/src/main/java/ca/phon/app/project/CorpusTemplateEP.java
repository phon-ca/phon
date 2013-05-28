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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.MissingResourceException;

import ca.phon.application.project.IPhonProject;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.IUtterance;
import ca.phon.exceptions.ParserException;
import ca.phon.gui.recordeditor.RecordEditorModel;
import ca.phon.gui.recordeditor.SessionTemplateEditor;
import ca.phon.gui.recordeditor.SystemTierType;
import ca.phon.system.logger.PhonLogger;
import ca.phon.system.plugin.IPluginEntryPoint;
import ca.phon.system.plugin.PhonPlugin;

/**
 * Opens the editor for corpus templates.
 */
@PhonPlugin(name="default")
public class CorpusTemplateEP implements IPluginEntryPoint {

	/** Our window */
	private SessionTemplateEditor _mainWindow;

	/** The project (if the associated transcript is in a project.) */
	private IPhonProject project;
	
	/** Are we in multi-blind mode? */
	private boolean blindMode;
	
	/** The working transcript */
	private String corpusName;
	
	private final static String EP_NAME = "CorpusTemplate";
	@Override
	public String getName() {
		return EP_NAME;
	}

	/**
	 * Entry point for module
	 * @param initInfo
	 * @throws ModuleException
	 */
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		if(initInfo.get("corpus") == null
				|| initInfo.get("project") == null)
			throw new IllegalArgumentException("Aborting template editor.");

		project = (IPhonProject)initInfo.get("project");
		corpusName = initInfo.get("corpus").toString();

		// look for the template file, if not found create a new
		// transcript as a template
		String sessionName = "__sessiontemplate";
		String templateName = sessionName + ".xml";

		String templatePath =
				project.getProjectLocation() +
				File.separator +
				corpusName +
				File.separator +
				templateName;
		File templateFile = new File(templatePath);

		ITranscript template = null;
		if(templateFile.exists()) {
			try {
				// load template from file
				template = project.getTranscript(corpusName, sessionName);
			} catch (IOException ex) {
				PhonLogger.severe(ex.toString());
			}
		}

		// if file was not found, or we could not load template
		// create a new one
		if(template == null) {
			try {
				template = project.newTranscript(corpusName, sessionName);
				IUtterance utt = template.newUtterance();
				utt.setTierString(SystemTierType.Orthography.getTierName(), "");
			} catch (ParserException ex) {
				PhonLogger.warning(ex.toString());
			} catch (IOException ex) {
				PhonLogger.severe(ex.toString());
			}
		}

		// if still not loaded...
		if(template == null)
			throw new MissingResourceException("Could not load session template.", getClass().getName(), "template" );


		// create the editor model
		final RecordEditorModel model = new RecordEditorModel(project, template);

		// create window
		_mainWindow = new SessionTemplateEditor(model);
		_mainWindow.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent we) {
			}

			@Override
			public void windowClosing(WindowEvent we) {
			}

			@Override
			public void windowClosed(WindowEvent we) {
				// stop model EDT
//				model.shutdownEDT();
			}

			@Override
			public void windowIconified(WindowEvent we) {
			}

			@Override
			public void windowDeiconified(WindowEvent we) {
			}

			@Override
			public void windowActivated(WindowEvent we) {
			}

			@Override
			public void windowDeactivated(WindowEvent we) {
			}
		});
		_mainWindow.setBounds(0, 0, 500, 600);
//		_mainWindow.centerWindow();
		_mainWindow.setLocationByPlatform(true);
		_mainWindow.setVisible(true);
	}

}
