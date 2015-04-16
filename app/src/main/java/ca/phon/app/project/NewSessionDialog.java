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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PhonConstants;

public class NewSessionDialog extends JDialog {
	
	private static final long serialVersionUID = 8888896161322222665L;

	private final static Logger LOGGER = Logger.getLogger(NewSessionDialog.class.getName());
	
	private NewSessionPanel newSessionPanel;
	private JButton btnCreateSession = new JButton();
	private JButton btnCancel = new JButton();

	private Project proj;
		
	/**
	 * Default constructor
	 */
	public NewSessionDialog(Project project) {
		super();
		this.proj = project;
		setTitle("New Session");
		setModal(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		newSessionPanel = new NewSessionPanel(project);
		
		init();
	}
	
	/**
	 * Constructor. Default selects the corpus.
	 */
	public NewSessionDialog(Project project, String corpusName) {
		this(project);
		newSessionPanel.setSelectedCorpus(corpusName);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader(getTitle(), "Create a new Session");
		add(header, BorderLayout.NORTH);
		
		btnCreateSession.setActionCommand("Create");
		btnCreateSession.setName("btnCreateSession");
		btnCreateSession.setText("Ok");
		btnCreateSession.setDefaultCapable(true);
		btnCreateSession.addActionListener(new CreateSessionListener());
		getRootPane().setDefaultButton(btnCreateSession);
		
		btnCancel.setActionCommand("Cancel");
		btnCancel.setName("btnCancel");
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(new CancelListener());
		
		add(newSessionPanel, BorderLayout.CENTER);
		
		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(btnCreateSession, btnCancel);
		add(buttonBar, BorderLayout.SOUTH);
	}

	/**
	 * Create session button listener.
	 */
	private class CreateSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			// Ensure a non-empty corpus name (description is optional)
			String sessionName = newSessionPanel.getSessionName().trim();
			if (sessionName == null || sessionName.length() == 0) {
				showMessage(
					"New Session",
					"You must specify a non-empty session name!");
				return;
			}

			// make sure invalid characters are not present
			// make sure corpus name does not contain illegal characters
			boolean invalid = false;
			if(sessionName.indexOf('.') >= 0) {
				invalid = true;
			}
			for(char invalidChar:PhonConstants.illegalFilenameChars) {
				if(sessionName.indexOf(invalidChar) >= 0) {
					invalid = true;
					break;
				}
			}
			
			if(invalid) {
				showMessage(
						"New Session",
						"Session name includes illegal characters.");
				return;
			}
			
			String corpusName = (String) newSessionPanel.getSelectedCorpus();
			try {
				final Session session = proj.createSessionFromTemplate(corpusName, sessionName);
				
				NewSessionDialog.this.dispose();
				
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
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				showMessage(
						"New Session",
						"Could not create session. Reason: " + e.getMessage());
			}
		}
	}
	
	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(this);
		
		NativeDialogs.showDialog(props);
	}
	
	/**
	 * Cancel button listener.
	 */
	private class CancelListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			NewSessionDialog.this.dispose();
		}
	}
}