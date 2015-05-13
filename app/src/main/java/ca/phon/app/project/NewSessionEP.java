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

import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;


/**
 * New session module.
 * 
 */
@PhonPlugin(name="default")
public class NewSessionEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(NewSessionEP.class.getName());
	
	private Project proj;
	
	public final static String EP_NAME = "NewSession";
	
	public final static String PROJECT_WINDOW_PROP = "ProjectWindow";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		
		NativeDialogs.showDialog(props);
	}
	
	public void createSession(String corpusName, String sessionName)
		throws IOException {
		final Session session = proj.createSessionFromTemplate(corpusName, sessionName);
		
		// open the session
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", proj);
		initInfo.put("corpusName", corpusName);
		initInfo.put("session", session);
		
		try {
			PluginEntryPointRunner.executePlugin("SessionEditor", initInfo);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			
			showMessage("Open Session", 
					"Could not open session. Reason: " + e.getMessage());
		}
	}
	
	@Override
	public void pluginStart(final Map<String, Object> initInfo)  {
		final EntryPointArgs args = new EntryPointArgs(initInfo);
		proj = args.getProject();
		if(proj == null)
			throw new IllegalArgumentException("Project property not set.");
		
		final String corpusName = 
				( args.getCorpus() != null ? args.getCorpus() :
					(args.get(PROJECT_WINDOW_PROP) != null ? ((ProjectWindow)args.get(PROJECT_WINDOW_PROP)).getSelectedCorpus() : null));
		final String sessionName = (String)args.get(EntryPointArgs.SESSION_NAME);
		
		if(corpusName != null && sessionName != null) {
			// create session
			try {
				createSession(corpusName, sessionName);
			} catch (IOException e) {
				Toolkit.getDefaultToolkit().beep();
				showMessage("Unable to create session", e.getLocalizedMessage());
			}
		} else {
			final Runnable onEDT = new Runnable() {
				
				@Override
				public void run() {
					NewSessionDialog dlg = null;
					if(corpusName == null) {
						dlg = new NewSessionDialog(proj);
					} else {
						dlg = new NewSessionDialog(proj, corpusName);
					}
					dlg.setModal(true);
					dlg.pack();
					dlg.setVisible(true);
					
					if(!dlg.wasCanceled()) {
						// create session
						try {
							createSession(dlg.getCorpusName(), dlg.getSessionName());
						} catch (IOException e) {
							Toolkit.getDefaultToolkit().beep();
							showMessage("Unable to create session", e.getLocalizedMessage());
						}
					}
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				onEDT.run();
			else
				SwingUtilities.invokeLater(onEDT);
		}
		
	}
	
}
