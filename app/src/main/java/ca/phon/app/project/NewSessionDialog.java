/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.project;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PhonConstants;

public class NewSessionDialog extends JDialog {
	
	private static final long serialVersionUID = 8888896161322222665L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NewSessionDialog.class.getName());
	
	private NewSessionPanel newSessionPanel;
	private JButton btnCreateSession = new JButton();
	private JButton btnCancel = new JButton();

	private boolean canceled = false;
	
	private Project proj;

	/**
	 * Default constructor
	 */
	public NewSessionDialog(Project project) {
		super();
		this.proj = project;
		setTitle(project.getName() + " : New Session");
		setModal(true);
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
		
		SwingUtilities.invokeLater( () -> {
			newSessionPanel.setSelectedCorpus(corpusName);
		});
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader(getTitle(), "Create a new Session");
		add(header, BorderLayout.NORTH);
		
		btnCreateSession.setActionCommand("Create");
		btnCreateSession.setName("btnCreateSession");
		btnCreateSession.setText("Ok");
		btnCreateSession.setDefaultCapable(true);
		btnCreateSession.addActionListener( (e) -> {
			if(validateForm()) {
				canceled = false;
				dispose();
			}
		});
		getRootPane().setDefaultButton(btnCreateSession);
		
		btnCancel.setActionCommand("Cancel");
		btnCancel.setName("btnCancel");
		btnCancel.setText("Cancel");
		btnCancel.addActionListener( (e) -> {
			canceled = true;
			dispose();
		});
		
		add(newSessionPanel, BorderLayout.CENTER);
		
		JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(btnCreateSession, btnCancel);
		add(buttonBar, BorderLayout.SOUTH);
	}
	
	public boolean validateForm() {
		boolean valid = true;
		// Ensure a non-empty corpus name (description is optional)
		if (getSessionName() == null || getSessionName().length() == 0) {
			ToastFactory.makeToast(
				"You must specify a non-empty session name!").start(newSessionPanel);
			valid = false;
		}
		final String sessionName = getSessionName();
		
		if(sessionName.indexOf('.') >= 0) {
			valid = false;
		}
		for(char invalidChar:PhonConstants.illegalFilenameChars) {
			if(sessionName.indexOf(invalidChar) >= 0) {
				valid = false;
				break;
			}
		}
		
		if(!valid) {
			ToastFactory.makeToast(
					"Session name includes illegal characters.").start(newSessionPanel);
		}
		return valid;
	}

	public String getSessionName() {
		return newSessionPanel.getSessionName();
	}
	
	public String getCorpusName() {
		return newSessionPanel.getSelectedCorpus();
	}
	
	public boolean wasCanceled() {
		return canceled;
	}
	
}