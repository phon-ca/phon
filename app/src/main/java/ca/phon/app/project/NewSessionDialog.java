/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.util.logging.Logger;

import javax.swing.*;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PhonConstants;

public class NewSessionDialog extends JDialog {
	
	private static final long serialVersionUID = 8888896161322222665L;

	private final static Logger LOGGER = Logger.getLogger(NewSessionDialog.class.getName());
	
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